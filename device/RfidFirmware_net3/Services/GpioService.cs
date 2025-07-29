using RfidFirmware.Services.Interfaces;
using RfidFirmware.Models;
using System.Device.Gpio;
using System.Collections.Generic;
using System.Linq;
using Microsoft.Extensions.Logging;
using System;

namespace RfidFirmware.Services
{
    public class GpioService : IGpioService
    {
        private readonly List<Gpio> GpioList = new List<Gpio>();
        private readonly GpioController _controller;
        private readonly ILogger<GpioService> _logger;
        private readonly object Gpio1_5Lock = new object();
        private readonly object Gpio6Lock = new object();

        public GpioService(ILogger<GpioService> logger)
        {
            _logger = logger;
            _controller = new GpioController();

            GpioList.Add(new Gpio() { Nr = 1, PinNr = 102, State = false });
            GpioList.Add(new Gpio() { Nr = 2, PinNr = 104, State = false });
            GpioList.Add(new Gpio() { Nr = 3, PinNr = 101, State = false });
            GpioList.Add(new Gpio() { Nr = 4, PinNr = 9, State = false });
        }

        public void Init()
        {
            _controller.OpenPin(102, PinMode.Output, true);
            _controller.OpenPin(104, PinMode.Output, true);
            _controller.OpenPin(101, PinMode.Output, true);
            _controller.OpenPin(9, PinMode.Output, true);
        }

        public void SetGpio1_5(int number)
        {
            if (number < 6)
            {
                lock (Gpio1_5Lock)
                {
                    SetGpios(MapTagNrToGpios(number));
                }
            }
            else
            {
                throw new ArgumentOutOfRangeException("number", "Number must be between 1-5");
            }
        }

        public void SetGpio6(bool state)
        {
            _logger.LogDebug($"set gpio nr 4 (person) state {state}");
            lock (Gpio6Lock)
            {
                _controller.Write(9, !state);
            }
        }

        private List<Gpio> MapTagNrToGpios(int number)
        {
            List<Gpio> gpioList = new List<Gpio>
            {
                new Gpio() { Nr = 1, PinNr = 102, State = false },
                new Gpio() { Nr = 2, PinNr = 104, State = false },
                new Gpio() { Nr = 3, PinNr = 101, State = false },
            };

            if (number == 1)
            {
                var gpio = gpioList.FirstOrDefault(n => n.Nr == 1);
                if (gpio != null) gpio.State = true;
            }
            else if (number == 2)
            {
                var gpio = gpioList.FirstOrDefault(n => n.Nr == 2);
                if (gpio != null) gpio.State = true;
            }
            else if (number == 3)
            {
                var gpio1 = gpioList.FirstOrDefault(n => n.Nr == 1);
                var gpio2 = gpioList.FirstOrDefault(n => n.Nr == 2);
                if (gpio1 != null) gpio1.State = true;
                if (gpio2 != null) gpio2.State = true;
            }
            else if (number == 4)
            {
                var gpio = gpioList.FirstOrDefault(n => n.Nr == 3);
                if (gpio != null) gpio.State = true;
            }
            else if (number == 5)
            {
                var gpio1 = gpioList.FirstOrDefault(n => n.Nr == 3);
                var gpio2 = gpioList.FirstOrDefault(n => n.Nr == 1);
                if (gpio1 != null) gpio1.State = true;
                if (gpio2 != null) gpio2.State = true;
            }

            return gpioList;
        }

        private void SetGpios(List<Gpio> gpios)
        {
            foreach (var gpio in gpios)
            {
                _logger.LogDebug($"set gpio nr {gpio.Nr} state {gpio.State}");
                _controller.Write(gpio.PinNr, !gpio.State);
            }
        }
    }
}
