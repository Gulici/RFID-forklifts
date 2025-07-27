using Timer = System.Timers.Timer;
using System.Timers;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Configuration;
using Microsoft.Extensions.Options;
using Microsoft.Extensions.Logging; 
using System.Collections.Generic;



namespace RfidFirmware.Mocks
{
    public class MockRfidService : IRfidService
    {
        private readonly ILogger<MockRfidService> _logger;
        private readonly ReaderSettings _settings;
        private readonly Timer _timer;
        private int _currentIndex = 0;
        private readonly List<string> _epcs;

        public event TagReadHandler? TagRead;
        public event ConnectionHandler? ReaderConnectionEvent;

        public MockRfidService(ILogger<MockRfidService> logger, IOptions<ReaderSettings> settings)
        {
            _logger = logger;
            _settings = settings.Value;

            _epcs = new List<string>
            {
                _settings.TagEpc_1,
                _settings.TagEpc_2,
                _settings.TagEpc_3,
                _settings.TagEpc_4,
                _settings.TagEpc_5,
                _settings.TagEpc_6
            };

            _timer = new System.Timers.Timer(5000);
            _timer.Elapsed += OnTimerElapsed;
        }

        private void OnTimerElapsed(object? sender, ElapsedEventArgs e)
        {
            var epc = _epcs[_currentIndex];
            _currentIndex = (_currentIndex + 1) % _epcs.Count;

            var mockTag = new Tag
            {
                Epc = epc,
                AntennaNr = 1
            };

            _logger.LogInformation("Mock tag read: {Epc}", mockTag.Epc);
            TagRead?.Invoke(mockTag);
        }

        public void InitReader()
        {
            _logger.LogInformation("Mock RFID reader initialized.");
            ReaderConnectionEvent?.Invoke(true);
        }

        public void StartInventory()
        {
            _logger.LogInformation("Mock inventory started.");
            _timer.Start();
        }

        public void StopInventory()
        {
            _logger.LogInformation("Mock inventory stopped.");
            _timer?.Stop();
        }

        public void Disconnect()
        {
            _logger.LogInformation("Mock RFID reader disconnected.");
            ReaderConnectionEvent?.Invoke(false);
        }

        public int GetLastLoggTimeoutSec() => 0;
    }
}
