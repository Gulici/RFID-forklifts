using Reader;
using RfidFirmware.Configuration;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Models;
using Microsoft.Extensions.Options;
using Microsoft.Extensions.Logging;
using System;
using System.Collections.Generic;

namespace RfidFirmware.Services
{
    public class RfidServiceSpider : IRfidService
    {
        public event TagReadHandler TagRead = delegate { };
        public event ConnectionHandler ReaderConnectionEvent = delegate { };

        private readonly ReaderSettings _settings;
        private ReaderMethod _reader;
        private readonly ILogger<RfidServiceSpider> _logger;
        private DateTime _lastInventoryLoop = DateTime.UtcNow;
        private byte[] _btAryData_4 = new byte[10];
        private byte[] _antennasPowers = new byte[] { 30, 30, 30, 30, 30, 30, 30, 30 };
        private bool _isLoop = false;

        private int _fastSwitchCount = 0;
        private DateTime _lastFastSwitchLog = DateTime.UtcNow;

        public RfidServiceSpider(IOptions<ReaderSettings> settings, ILogger<RfidServiceSpider> logger)
        {
            _settings = settings.Value;
            _logger = logger;
        }

        public void Disconnect()
        {
             try
            {
                _reader.CloseCom();
                _logger.LogInformation("COM port closed successfully.");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception occurred while closing COM port.");
            }
        }

        public int GetLastLoggTimeoutSec()
        {
            return (int)(DateTime.UtcNow - _lastInventoryLoop).TotalSeconds;
        }

        public void InitReader()
        {
            _reader = new ReaderMethod();
            ConnectReader(_settings.ReaderPort);
            _reader.m_OnInventoryTag = OnInventoryTag;
            _reader.m_OnExeCMDStatusValues = OnExeCMDStatusValues;
            _reader.m_OnFastSwitchAntInventoryTagEnd = OnFastSwitchAntInventoryTagEnd;
            SetAntennaPower(_settings.Power);
        }

        public void StartInventory()
        {
            var enableAntennas = _settings.EnableAntennas;
            _logger.LogDebug("EnableAntennas: " + string.Join(", ", enableAntennas));
            _btAryData_4 = new byte[18];
            int counter = 0;

            for (int i = 0; i < 8; i++)
            {
                if (enableAntennas[i])
                {
                    _btAryData_4[counter++] = (byte)i;
                    _btAryData_4[counter++] = 1;
                }
                else
                {
                    _btAryData_4[counter++] = 0xFF;
                    _btAryData_4[counter++] = 0;
                }
            }
            _btAryData_4[16] = 0x00;
            _btAryData_4[17] = 10;
            
            _reader.FastSwitchInventory(0xFF, _btAryData_4);
            _isLoop = true;
            _logger.LogInformation("Inventory started");
            _lastInventoryLoop = DateTime.UtcNow;
        }

        public void StopInventory()
        {   
            _isLoop = false;
        }

        private void ConnectReader(string port)
        {
            string strException;
            int baudrate = 115200;
            int nRet = _reader.OpenCom(port, baudrate, out strException);
            if (nRet != 0)
            {
                var msg = $"Connection failed, failure cause: {strException}";
                throw new Exception(msg);
            }
            else
            {
                _logger.LogInformation($"Connected to {port} at {baudrate}");
            }
        }

        private void OnExeCMDStatusValues(byte cmd, byte statusCode, byte[] arrayValues)
        {
            _logger.LogDebug($"CMD: {cmd}, Status: {statusCode}, Values: {BitConverter.ToString(arrayValues)}");
        }

        private void OnInventoryTag(RXInventoryTag tag)
        {
            tag.strEPC = tag.strEPC.Replace(" ", "").ToUpper();
            if (TagRead != null && (String.IsNullOrEmpty(_settings.TagMask) || tag.strEPC.StartsWith(_settings.TagMask)))
            {
                TagRead.Invoke(new Tag()
                {
                    Epc = tag.strEPC,
                    AntennaNr = tag.btAntId
                });
            }
        }

        private void OnFastSwitchAntInventoryTagEnd(RXFastSwitchAntInventoryTagEnd tagend)
        {
            if (_isLoop)
            {
                _reader.FastSwitchInventory(0xFF, _btAryData_4);
                _lastInventoryLoop = DateTime.UtcNow;
                _logger.LogDebug("Inventory restarted");
            }
            else
            {
                _logger.LogWarning("Inventory STOP");
            }
        }

        public void SetAntennaPower(List<int> antennasPower)
        {
            for (int i = 0; i < antennasPower.Count && i < _antennasPowers.Length; i++)
            {
                _antennasPowers[i] = (byte)antennasPower[i];
            }
            _reader.SetOutputPower(0xFF, _antennasPowers);
        }
    }
}
