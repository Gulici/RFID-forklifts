namespace RfidFirmware.Services.Interfaces
{
    public interface IGpioService
    {
        void Init();
        void SetGpio1_5(int number);
        void SetGpio6(bool state);
    }
}