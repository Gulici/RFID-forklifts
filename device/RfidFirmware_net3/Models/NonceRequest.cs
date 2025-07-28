namespace RfidFirmware.Models
{
    public class NonceRequest
    {
        public NonceRequest(string PublicKeyPem)
        {
            this.PublicKeyPem = PublicKeyPem;
        }

        public string PublicKeyPem { get; set; }
    }
}