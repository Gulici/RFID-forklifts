using RfidFirmware.Services.Interfaces;

namespace RfidFirmware;

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
