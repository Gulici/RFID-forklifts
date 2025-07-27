using RfidFirmware.Services.Interfaces;
using RfidFirmware.Models;
using System.Device.Gpio;

namespace RfidFirmware.Services
{
    public class GpioService : IGpioService
    {
        private readonly List<Gpio> GpioList = [];
        private readonly GpioController _controller;
        private readonly ILogger<GpioService> _logger;

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
                SetGpios(MapTagNrToGpios(number));
            }
            else
                throw new ArgumentOutOfRangeException("Number must be beetwen 1-5");
        }

        public void SetGpio6(bool state)
        {
            _logger.LogDebug($"set gpio nr 4 (person) state {state}");
            _controller.Write(9, !state);
        }

        private List<Gpio> MapTagNrToGpios(int number)
        {
            List<Gpio> gpioList =
            [
                new Gpio() { Nr = 1, PinNr = 102, State = false },
                new Gpio() { Nr = 2, PinNr = 104, State = false },
                new Gpio() { Nr = 3, PinNr = 101, State = false },
            ];

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