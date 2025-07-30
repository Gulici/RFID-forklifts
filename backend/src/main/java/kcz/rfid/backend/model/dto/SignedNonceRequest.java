package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload containing the signed nonce for verification")
public class SignedNonceRequest {
    @Schema(description = "Public key in PEM format", example = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkq...\n-----END PUBLIC KEY-----")
    private String publicKeyPem;
    @Schema(description = "Base64 encoded signature of the nonce created with private key", example = "MEUCIQDaT2q...")
    private String signatureBase64;
}
