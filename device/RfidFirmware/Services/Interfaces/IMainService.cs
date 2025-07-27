namespace RfidFirmware.Services.Interfaces
{
    public interface IMainService
    {
        Task RunAsync(CancellationToken stoppingToken);
    }
}