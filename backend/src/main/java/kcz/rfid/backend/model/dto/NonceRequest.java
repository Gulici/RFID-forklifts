package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload containing a public key in PEM format for nonce generation")
public class NonceRequest {
    @Schema(description = "Public key in PEM format", example = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkq...\n-----END PUBLIC KEY-----")
    String publicKeyPem;
}
