using System.Threading.Tasks;

namespace RfidFirmware.Services.Interfaces
{
    public interface IApiService
    {
        Task<bool> RegisterDeviceAsync(string login, string password, string deviceName, string publicKey);
        Task<bool> LoginDeviceAsync();
    }
}