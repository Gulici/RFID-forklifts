package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class SignedNonceRequest {
    private String publicKeyPem;
    private String signatureBase64;
}
