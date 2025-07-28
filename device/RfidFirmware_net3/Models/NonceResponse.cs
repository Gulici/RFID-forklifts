namespace RfidFirmware.Models
{
    public class NonceResponse
    {
        public NonceResponse(string Nonce)
        {
            this.Nonce = Nonce;
        }

        public string Nonce { get; set; }
    }
}