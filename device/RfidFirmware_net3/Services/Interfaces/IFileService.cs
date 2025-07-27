using System.Threading.Tasks;

namespace RfidFirmware.Services.Interfaces
{
    public interface IFileService
    {
        int ReadGpioNr();
        Task SaveGpioNrAsync(int gpioNr);
    }
}
