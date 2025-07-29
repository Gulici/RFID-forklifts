using RfidFirmware.Services.Interfaces;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System.Threading;
using System.Threading.Tasks;
using RfidFirmware.Configuration;
using System;

namespace RfidFirmware
{
    #nullable enable
    public class Worker : BackgroundService
    {
        private readonly ILogger<Worker> _logger;
        private readonly IMainService _mainService;
        private readonly IRegisterService _registerService;
        private readonly AppFlags _flags;
        private readonly IHostApplicationLifetime _appLifetime;

        public Worker(
            ILogger<Worker> logger,
            AppFlags appFlags,
            IRegisterService registerService,
            IMainService mainService,
            IHostApplicationLifetime appLifetime)
        {
            _logger = logger;
            _mainService = mainService;
            _registerService = registerService;
            _flags = appFlags;
            _appLifetime = appLifetime;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _appLifetime.ApplicationStarted.Register(() =>
            {
                _logger.LogInformation("RFIDFirmware started");
            });

            if (_flags.IsRegister)
            {
                await _registerService.RegisterAsync();
            }
            else
            {
                await _mainService.RunAsync(stoppingToken);
            }
        }
    }
}

