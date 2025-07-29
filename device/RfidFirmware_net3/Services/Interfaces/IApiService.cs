using System.Threading.Tasks;
using RfidFirmware.Models;

namespace RfidFirmware.Services.Interfaces
{
    public interface IApiService
    {
        Task<bool> RegisterDeviceAsync(string login, string password, string deviceName, string publicKey);
        Task<bool> LoginDeviceAsync();
        Task<bool> SendTagAsync(Tag tag);
    }
}