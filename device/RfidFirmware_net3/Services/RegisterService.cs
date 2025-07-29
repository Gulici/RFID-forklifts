using System;
using System.IO;
using System.Text;
using System.Text.Json;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Hosting;
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
        private readonly IHostApplicationLifetime _appLifetime;

        public RegisterService
            (IApiService apiService,
            ILogger<RegisterService> logger,
            IHostApplicationLifetime appLifetime)
        {
            _apiService = apiService;
            _logger = logger;
            _appLifetime = appLifetime;
        }
        public async Task RegisterAsync()
        {
            Console.Write("Username: ");
            var login = Console.ReadLine();
            Console.Write("Password: ");
            var password = Console.ReadLine();
            Console.Write("Enter new device name: ");
            var deviceName = Console.ReadLine();

            var publicKey = RsaKeyUtils.GenerateKeys();

            bool success = await _apiService.RegisterDeviceAsync(login, password, deviceName, publicKey);
            if (!success)
            {
                _logger.LogWarning("Unsuccessful register.");
                _appLifetime.StopApplication();
                return;
            }

            var info = new DeviceInfo { DeviceName = deviceName, Registered = true };
            Directory.CreateDirectory("config");
            File.WriteAllText("config/device_info.json", JsonSerializer.Serialize(info));
            _logger.LogInformation("Device registered successfully");
            _appLifetime.StopApplication();
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