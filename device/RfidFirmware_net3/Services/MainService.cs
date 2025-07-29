using RfidFirmware.Services.Interfaces;
using RfidFirmware.Configuration;
using RfidFirmware.Models;
using Microsoft.Extensions.Options;
using Microsoft.Extensions.Logging;
using System;
using System.Threading.Tasks;
using System.Threading;


namespace RfidFirmware.Services
{
    public class MainService : IMainService
    {
        private readonly ILogger<MainService> _logger;
        private readonly IGpioService _gpioService;
        private readonly IRfidService _rfidService;
        private readonly IFileService _fileService;
        private readonly ITagHandler _tagHandler;
        private readonly ReaderSettings _settings;

        public MainService
            (ILogger<MainService> logger,
            IGpioService gpioService,
            IRfidService rfidService,
            IOptions<ReaderSettings> settings,
            IFileService fileService,
            ITagHandler tagHandler)
        {
            _logger = logger;
            _gpioService = gpioService;
            _rfidService = rfidService;
            _fileService = fileService;
            _tagHandler = tagHandler;
            _settings = settings.Value;
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

                _tagHandler.CheckAndResetGpio6IfTimeout();

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

        private void _rfidService_TagRead(Tag tag)
        {
            _logger.LogDebug($"READED EPC  : {tag.Epc} antenna: {tag.AntennaNr}");
            var gpioNr = MapTagEpcToGpio(tag);
            _tagHandler.HandleTag(tag, gpioNr);
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
    }
}