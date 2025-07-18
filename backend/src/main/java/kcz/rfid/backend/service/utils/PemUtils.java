package kcz.rfid.backend.service.utils;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PemUtils {
    public static PublicKey parsePublicKeyPem(String pem) throws Exception {
        String publicKeyPEM = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static String computeFingerprint(String publicKeyPem) {
        try {
            PublicKey key = parsePublicKeyPem(publicKeyPem);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digest = sha256.digest(key.getEncoded());
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Could not compute fingerprint", e);
        }
    }

    public static String getPemPublicKey(PublicKey publicKey) {
        byte[] encoded = publicKey.getEncoded();
        return  "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded) +
                "\n-----END PUBLIC KEY-----";

    }

    public static String getPemPrivateKey(PrivateKey privateKey) {
        byte[] encoded = privateKey.getEncoded();
        return  "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded) +
                "\n-----END PRIVATE KEY-----";
    }
}
