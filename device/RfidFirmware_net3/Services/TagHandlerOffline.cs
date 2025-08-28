using System;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RfidFirmware.Configuration;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;

namespace RfidFirmware.Services
{
    public class OfflineTagHandler : ITagHandler
    {
        private readonly ILogger<OfflineTagHandler> _logger;
        private readonly IGpioService _gpioService;
        private readonly IFileService _fileService;
        private readonly ReaderSettings _settings;
        private readonly int _InventoryTimeout;

        private string _readedEpc = "";
        private string _readedEpc6 = "";
        private DateTime _lastReadTag6 = DateTime.Now;

        public OfflineTagHandler
            (ILogger<OfflineTagHandler> logger,
            IGpioService gpioService,
            IFileService fileService,
            IOptions<ReaderSettings> settings)
        {
            _logger = logger;
            _gpioService = gpioService;
            _fileService = fileService;
            _settings = settings.Value;
            _InventoryTimeout = _settings.Timeout;
        }

        public void HandleTag(Tag tag, int gpioNr)
        {
            if (gpioNr > 0)
            {
                if (gpioNr < 6 && !_readedEpc.Contains(tag.Epc))
                {
                    _readedEpc = tag.Epc;
                    _gpioService.SetGpio1_5(gpioNr);
                    _fileService.SaveGpioNrAsync(gpioNr);
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

        public void SendLastTag()
        {
        }

        public void CheckAndResetGpio6IfTimeout()
        {
            if (!string.IsNullOrEmpty(_readedEpc6) &&
                    _lastReadTag6.AddMilliseconds(_InventoryTimeout) < DateTime.UtcNow)
            {
                _readedEpc6 = string.Empty;
                _gpioService.SetGpio6(false);
            }
        }
    }
}