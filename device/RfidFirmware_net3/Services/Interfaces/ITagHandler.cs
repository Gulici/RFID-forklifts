using RfidFirmware.Models;

namespace RfidFirmware.Services.Interfaces
{
    public interface ITagHandler
    {
        void HandleTag(Tag tag, int gpioNr);
        public void CheckAndResetGpio6IfTimeout();
        public void SendLastTag();
    }

}