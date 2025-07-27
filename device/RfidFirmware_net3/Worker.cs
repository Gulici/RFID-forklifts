using RfidFirmware.Services.Interfaces;
using Microsoft.Extensions.Hosting;      
using Microsoft.Extensions.Logging;     
using System.Threading;
using System.Threading.Tasks; 
   
namespace RfidFirmware
{
    public class Worker : BackgroundService
    {
        private readonly ILogger<Worker> _logger;
        private readonly IMainService _mainService;

        public Worker(ILogger<Worker> logger, IMainService mainService)
        {
            _logger = logger;
            _mainService = mainService;
        }

        protected override async Task ExecuteAsync(CancellationToken stoppingToken)
        {
            _logger.LogInformation("RFIDFirmware started");
            await _mainService.RunAsync(stoppingToken);
        }
    }
    
}

