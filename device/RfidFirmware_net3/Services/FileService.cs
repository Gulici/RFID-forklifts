using System;
using System.IO;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using RfidFirmware.Services.Interfaces;

namespace RfidFirmware.Services
{
    public class FileService : IFileService
    {
        private readonly ILogger<FileService> _logger;

        public FileService(ILogger<FileService> logger)
        {
            _logger = logger;
        }

        public int ReadGpioNr()
        {
            try
            {
                if (!File.Exists("gpio.txt"))
                    return 0;

                var line = File.ReadAllText("gpio.txt");
                if (!string.IsNullOrWhiteSpace(line) && int.TryParse(line, out var gpioNr))
                    return gpioNr;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception: {Message}", ex.Message);
            }

            return 0;
        }

        public async Task SaveGpioNrAsync(int gpioNr)
        {
            try
            {
                await File.WriteAllTextAsync("gpio.txt", gpioNr.ToString());
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Exception: {Message}", ex.Message);
            }
        }
    }
}
