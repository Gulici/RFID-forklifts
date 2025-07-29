using System;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using RfidFirmware.Configuration;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;

namespace RfidFirmware.Services
{
    public class OnlineTagHandler : ITagHandler
    {
        private readonly ILogger<OnlineTagHandler> _logger;
        private readonly IGpioService _gpioService;
        private readonly IFileService _fileService;
        private readonly IApiService _apiService;
        private readonly ReaderSettings _settings;
        private readonly int _InventoryTimeout;

        private string _readedEpc = "";
        private string _readedEpc6 = "";
        private DateTime _lastReadTag6 = DateTime.Now;

        public OnlineTagHandler
            (ILogger<OnlineTagHandler> logger,
            IGpioService gpioService,
            IFileService fileService,
            IApiService apiService,
            IOptions<ReaderSettings> settings)
        {
            _logger = logger;
            _gpioService = gpioService;
            _fileService = fileService;
            _apiService = apiService;
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
                    _apiService.SendTagAsync(tag);
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