package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class NonceResponse {
    private String nonce;

    public NonceResponse(String nonce) {
        this.nonce = nonce;
    }
}
