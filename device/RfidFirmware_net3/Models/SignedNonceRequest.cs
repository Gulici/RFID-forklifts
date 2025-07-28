namespace RfidFirmware.Models
{
    public class SignedNonceRequest
    {
        public SignedNonceRequest(string PublicKeyPem, string SignatureBase64)
        {
            this.PublicKeyPem = PublicKeyPem;
            this.SignatureBase64 = SignatureBase64;
        }

        public string PublicKeyPem { get; set; }
        public string SignatureBase64 { get; set; }
    }
}