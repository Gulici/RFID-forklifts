using Reader;
using RfidFirmware.Configuration;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Models;
using Microsoft.Extensions.Options;

namespace RfidFirmware.Services
{
    public class RfidServiceSpider : IRfidService
    {
        public event TagReadHandler TagRead = delegate { };
        public event ConnectionHandler ReaderConnectionEvent = delegate { };
        ReaderSettings _settings;
        private ReaderMethod _reader = null!;
        private ILogger<RfidServiceSpider> _logger;
        private DateTime _lastInventoryLoop = DateTime.UtcNow;
        private byte[] _btAryData_4 = new byte[10];
        private byte[] _antennasPowers = new byte[] { 30, 30, 30, 30, 30, 30, 30, 30 };
        bool _isLoop = false;

        public RfidServiceSpider(IOptions<ReaderSettings> settings, ILogger<RfidServiceSpider> logger)
        {
            _settings = settings.Value;
            _logger = logger;
        }

        public void Disconnect()
        {
            _logger.Log(LogLevel.Information, "Reader disconnected");
        }

        public int GetLastLoggTimeoutSec()
        {
            return (int)(DateTime.UtcNow - _lastInventoryLoop).TotalSeconds;
        }

        public void InitReader()
        {
            _reader = new Reader.ReaderMethod();
            ConnectReader(_settings.ReaderPort);
            _reader.m_OnInventoryTag = OnInventoryTag;
            _reader.m_OnExeCMDStatusValues = OnExeCMDStatusValues;
            _reader.m_OnFastSwitchAntInventoryTagEnd = OnFastSwitchAntInventoryTagEnd;
            SetAntennaPower(_settings.Power);
        }

        public void StartInventory()
        {
            List<bool> enableAntennas = _settings.EnableAntennas;
            _btAryData_4 = new byte[18];
            int counter = 0;
            for (int i = 0; i < 8; i++)
            {
                if (enableAntennas[i])
                {
                    _btAryData_4[counter] = Convert.ToByte(i);
                    _btAryData_4[++counter] = Convert.ToByte(1);
                }
                else
                {
                    _btAryData_4[counter] = 0xFF;
                    _btAryData_4[++counter] = Convert.ToByte(0);
                }
                counter++;
            }
            _btAryData_4[16] = 0x00;
            _btAryData_4[17] = Convert.ToByte(10);
            _reader.FastSwitchInventory((byte)0xFF, _btAryData_4);
            _isLoop = true;
            _logger.LogWarning("Inventoy started");
            _lastInventoryLoop = DateTime.UtcNow;
        }

        public void StopInventory()
        {
            _isLoop = false;
        }

        private void ConnectReader(string port)
        {
            string strException = string.Empty;
            string strComPort = port;
            int nBaudrate = 115200;
            int nRet = _reader.OpenCom(strComPort, nBaudrate, out strException);
            if (nRet != 0)
            {
                string strLog = $"Connection failed, failure cause: {strException}";
                throw new Exception(strLog);
            }
            else
            {
                _logger.LogInformation($"Connected to {strComPort} at {nBaudrate}");
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

        void OnFastSwitchAntInventoryTagEnd(RXFastSwitchAntInventoryTagEnd tagend)
        {
            if (_isLoop)
            {
                _reader.FastSwitchInventory((byte)0xFF, _btAryData_4);
                _lastInventoryLoop = DateTime.UtcNow;
                _logger.LogWarning("Inventory started");
            }
            else
            {
                _logger.LogWarning("Inventory STOP");
            }
        }
        
        public void SetAntennaPower(List<int> antennasPower)
        {

            for (int i = 0; i < antennasPower.Count; i++)
            {
                _antennasPowers[i] = (byte)antennasPower[i];
            }
            _reader.SetOutputPower((byte)0xFF, _antennasPowers);
        }
    }
}