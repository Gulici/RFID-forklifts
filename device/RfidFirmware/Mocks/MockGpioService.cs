using RfidFirmware.Services.Interfaces;
using Microsoft.Extensions.Logging;

namespace RfidFirmware.Mocks
{
    public class MockGpioService(ILogger<MockGpioService> logger) : IGpioService
    {
        private readonly ILogger<MockGpioService> _logger = logger;

        public void Init()
        {
            _logger.LogInformation("Mock GPIO initialized.");
        }

        public void SetGpio1_5(int number)
        {
            _logger.LogInformation("Mock: SetGpio1_5 called with value: {Number}", number);
        }

        public void SetGpio6(bool state)
        {
            _logger.LogInformation("Mock: SetGpio6 called with state: {State}", state);
        }
    }
}
