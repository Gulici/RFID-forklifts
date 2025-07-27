using RfidFirmware.Services.Interfaces;
using RfidFirmware.Configuration;
using RfidFirmware.Models;
using Microsoft.Extensions.Options;

namespace RfidFirmware.Services
{
    public class MainServiceOffline : IMainService
    {
        private readonly ILogger<MainServiceOffline> _logger;
        private readonly IGpioService _gpioService;
        private readonly IRfidService _rfidService;
        private readonly IFileService _fileService;
        private readonly ReaderSettings _settings;
        private readonly int _InventoryTimeout;

        private string _readedEpc = "";
        private string _readedEpc6 = "";
        private DateTime _lastReadTag6 = DateTime.Now;

        public MainServiceOffline(ILogger<MainServiceOffline> logger, IGpioService gpioService, IRfidService rfidService, IOptions<ReaderSettings> settings, IFileService fileService)
        {
            _logger = logger;
            _gpioService = gpioService;
            _rfidService = rfidService;
            _fileService = fileService;
            _settings = settings.Value;
            _InventoryTimeout = _settings.Timeout;
        }

        public async Task RunAsync(CancellationToken stoppingToken)
        {
            int lastGpio;
            try
            {
                lastGpio = _fileService.ReadGpioNr();
                _logger.LogDebug($"Reader GPIO NR: {lastGpio}");
            }
            catch (Exception ex)
            {
                _logger.LogWarning(ex, "Failed to read last GPIO from file. Using default = 0.");
                lastGpio = 0;
            }

            _gpioService.Init();

            if (lastGpio > 0)
            {
                _gpioService.SetGpio1_5(lastGpio);
            }

            _rfidService.InitReader();
            _rfidService.TagRead += _rfidService_TagRead;
            _rfidService.StartInventory();

            while (!stoppingToken.IsCancellationRequested)
            {
                await Task.Delay(1000, stoppingToken);

                if (!string.IsNullOrEmpty(_readedEpc6) &&
                    _lastReadTag6.AddMilliseconds(_InventoryTimeout) < DateTime.UtcNow)
                {
                    _readedEpc6 = string.Empty;
                    _gpioService.SetGpio6(false);
                }

                if (_rfidService.GetLastLoggTimeoutSec() > 10)
                {
                    _logger.LogDebug("stop inventory Timeout");
                    _rfidService.StopInventory();
                    await Task.Delay(1000, stoppingToken);
                    _logger.LogDebug("start inventory Timeout");
                    _rfidService.StartInventory();
                }
            }
        }

        private void SaveLastGpioAsync(int gpioNr)
        {
            _fileService.SaveGpioNrAsync(gpioNr);
        }

        private int MapTagEpcToGpio(Tag tag)
        {
            bool antennaFor_1_5 = _settings.AntennasForGpios1_3[tag.AntennaNr - 1];
            bool antennaFor_6 = _settings.AntennasForGpio4[tag.AntennaNr - 1];
            string epc = tag.Epc;

            return epc switch
            {
                var e when e == _settings.TagEpc_1 && antennaFor_1_5 => 1,
                var e when e == _settings.TagEpc_2 && antennaFor_1_5 => 2,
                var e when e == _settings.TagEpc_3 && antennaFor_1_5 => 3,
                var e when e == _settings.TagEpc_4 && antennaFor_1_5 => 4,
                var e when e == _settings.TagEpc_5 && antennaFor_1_5 => 5,
                var e when e == _settings.TagEpc_6 && antennaFor_6 => 6,
                _ => 0
            };
        }
        
        private void _rfidService_TagRead(Tag tag)
        {
            _logger.LogDebug($"READED EPC  : {tag.Epc} antenna: {tag.AntennaNr}");
            var gpioNr = MapTagEpcToGpio(tag);
            if (gpioNr > 0)
            {
                if (gpioNr < 6 && !_readedEpc.Contains(tag.Epc))
                {
                    _readedEpc = tag.Epc;
                    _gpioService.SetGpio1_5(gpioNr);
                    SaveLastGpioAsync(gpioNr);
                }
                else if (gpioNr == 6)
                {
                    _lastReadTag6 = DateTime.UtcNow;
                    if (!_readedEpc6.Contains(tag.Epc))
                    {
                        _readedEpc6 = tag.Epc;
                        _gpioService.SetGpio6(true);
                    }
                }
            }
        }
    }
}