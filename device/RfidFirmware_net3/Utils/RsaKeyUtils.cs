using System;
using System.Linq;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace RfidFirmware.Utils
{
    public static class RsaKeyUtils
    {
        // private static readonly string KeyDir = Path.Combine(AppContext.BaseDirectory, "keys");
        // private static readonly string PrivateKeyPath = Path.Combine(KeyDir, "device_private.pem");
        // private static readonly string PublicKeyPath = Path.Combine(KeyDir, "device_public.pem");
        private static readonly string KeyDir = "keys";
        private static readonly string PrivateKeyPath = "keys/device_private.pem";
        private static readonly string PublicKeyPath = "keys/device_public.pem";
        public static string GenerateKeys()
        {
            using var rsa = RSA.Create(2048);
            var privateKey = ConvertToPem(rsa.ExportRSAPrivateKey(), "RSA PRIVATE KEY");
            var publicKey = ConvertToPem(rsa.ExportSubjectPublicKeyInfo(), "PUBLIC KEY");

            Directory.CreateDirectory(KeyDir);
            File.WriteAllText(PrivateKeyPath, privateKey);
            File.WriteAllText(PublicKeyPath, publicKey);

            return publicKey;
        }

        public static string GetPublicKeyPem()
        {
            if (!File.Exists(PublicKeyPath))
                throw new FileNotFoundException("Public key file not found", PublicKeyPath);

            return File.ReadAllText(PublicKeyPath);
        }

        public static string SignNonce(string nonce)
        {
            if (!File.Exists(PrivateKeyPath))
                throw new FileNotFoundException("Private key file not found", PrivateKeyPath);

            var privateKeyPem = File.ReadAllText(PrivateKeyPath);
            using var rsa = CreateRsaFromPrivateKeyPem(privateKeyPem);

            var dataBytes = Encoding.UTF8.GetBytes(nonce);
            var signatureBytes = rsa.SignData(dataBytes, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);
            
            return Convert.ToBase64String(signatureBytes);
        }

        private static string ConvertToPem(byte[] data, string label)
        {
            var base64 = Convert.ToBase64String(data, Base64FormattingOptions.InsertLineBreaks);
            return $"-----BEGIN {label}-----\n{base64}\n-----END {label}-----";
        }

        private static RSA CreateRsaFromPrivateKeyPem(string privateKeyPem)
        {
            var keyLines = privateKeyPem
                .Split('\n')
                .Where(line => !line.StartsWith("-----"))
                .ToArray();
            var keyBase64 = string.Join("", keyLines);
            var keyBytes = Convert.FromBase64String(keyBase64);

            var rsa = RSA.Create();
            rsa.ImportRSAPrivateKey(keyBytes, out _);
            return rsa;
        }
    }
}