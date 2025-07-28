using System;
using System.IO;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Utils;

namespace RfidFirmware.Services
{
    public class RegisterService : IRegisterService
    {
        private readonly IApiService _apiService;
        private readonly ILogger<RegisterService> _logger;

        public RegisterService(IApiService apiService, ILogger<RegisterService> logger)
        {
            _apiService = apiService;
            _logger = logger;
        }
        public async Task RegisterAsync()
        {
            Console.Write("Login: ");
            var login = Console.ReadLine();
            Console.Write("Hasło: ");
            var password = ReadPassword();
            Console.Write("Nazwa urządzenia: ");
            var deviceName = Console.ReadLine();

            var publicKey = RsaKeyUtils.GenerateKeys();

            bool success = await _apiService.RegisterDeviceAsync(login, password, deviceName, publicKey);
            if (!success)
            {
                _logger.LogWarning("Unsuccessful register.");
                return;
            }

            var info = new DeviceInfo { DeviceName = deviceName, Registered = true };
            Directory.CreateDirectory("config");
            File.WriteAllText("config/device_info.json", JsonSerializer.Serialize(info));
            _logger.LogInformation("Device registered succesfully");
        }

        private static string ReadPassword()
        {
            var pwd = new StringBuilder();
            ConsoleKey key;
            while ((key = Console.ReadKey(true).Key) != ConsoleKey.Enter)
            {
                if (key == ConsoleKey.Backspace && pwd.Length > 0)
                {
                    pwd.Length--;
                    Console.Write("\b \b");
                }
                else if (!char.IsControl((char)key))
                {
                    pwd.Append((char)key);
                    Console.Write("*");
                }
            }
            Console.WriteLine();
            return pwd.ToString();
        }
    }
}