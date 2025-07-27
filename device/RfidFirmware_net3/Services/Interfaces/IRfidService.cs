
using RfidFirmware.Models;

namespace RfidFirmware.Services.Interfaces
{
    public delegate void TagReadHandler(Tag tag);
    public delegate void ConnectionHandler(bool connected);
    public interface IRfidService
    {
        event TagReadHandler TagRead;
        event ConnectionHandler ReaderConnectionEvent;
        void InitReader();
        void StartInventory();
        void StopInventory();
        void Disconnect();
        int GetLastLoggTimeoutSec();
    }
}