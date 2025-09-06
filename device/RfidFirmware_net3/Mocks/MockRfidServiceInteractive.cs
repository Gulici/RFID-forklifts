using System;
using System.Threading;
using System.Threading.Tasks;
using RfidFirmware.Models;
using RfidFirmware.Services.Interfaces;
using RfidFirmware.Configuration;
using Microsoft.Extensions.Options;
using Microsoft.Extensions.Logging; 
using System.Collections.Generic;



namespace RfidFirmware.Mocks
{
    public class MockRfidServiceInteractive : IRfidService
    {
        private readonly ILogger<MockRfidService> _logger;
        private readonly ReaderSettings _settings;
        private CancellationTokenSource _cts;
        public event TagReadHandler TagRead;
        public event ConnectionHandler ReaderConnectionEvent;

        public MockRfidServiceInteractive(ILogger<MockRfidService> logger, IOptions<ReaderSettings> settings)
        {
            _logger = logger;
            _settings = settings.Value;
        }

        private void InputLoop(CancellationToken token)
        {
            while (!token.IsCancellationRequested)
            {
                Console.Write("Enter EPC (8 hex characters, press Enter to send): ");
                var input = Console.ReadLine();

                if (string.IsNullOrWhiteSpace(input))
                    continue;

                input = input.Trim();
                input = input.ToUpper();

                if (input.StartsWith(_settings.TagMask))
                {
                    Tag tag = new Tag
                    {
                        Epc = input,
                        AntennaNr = 1
                    };

                    _logger.LogInformation("Manual input EPC: {Epc}", tag.Epc);
                    TagRead.Invoke(tag);
                }
            }
        }

        public void InitReader()
        {
            _logger.LogInformation("Mock RFID reader initialized.");
            ReaderConnectionEvent?.Invoke(true);
        }

        public void StartInventory()
        {
            _logger.LogInformation("Mock inventory started.");
            _cts = new CancellationTokenSource();
            Task.Run(() => InputLoop(_cts.Token));
        }

        public void StopInventory()
        {
            _logger.LogInformation("Mock inventory stopped.");
            _cts?.Cancel();
        }

        public void Disconnect()
        {
            _logger.LogInformation("Mock RFID reader disconnected.");
            ReaderConnectionEvent?.Invoke(false);
        }

        public int GetLastLoggTimeoutSec() => 0;
    }
}
