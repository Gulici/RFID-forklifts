using System.Threading;
using System.Threading.Tasks;

namespace RfidFirmware.Services.Interfaces
{
    public interface IMainService
    {
        Task RunAsync(CancellationToken stoppingToken);
    }
}
