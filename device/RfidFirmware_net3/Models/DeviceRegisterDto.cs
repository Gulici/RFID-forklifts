namespace RfidFirmware.Models
{
    public class DeviceRegisterDto
    {

        public DeviceRegisterDto(string username, string password, string deviceName, string publicKey)
        {
            Username = username;
            Password = password;
            DeviceName = deviceName;
            PublicKey = publicKey;
        }

        public string Username { get; set; }
        public string Password { get; set; }
        public string DeviceName { get; set; }
        public string PublicKey { get; set; }
    }
}