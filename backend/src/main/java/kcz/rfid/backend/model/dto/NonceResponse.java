package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response payload containing the nonce string")
public class NonceResponse {
    @Schema(description = "Nonce value", example = "random-nonce-string-12345")
    private String nonce;

    public NonceResponse(String nonce) {
        this.nonce = nonce;
    }
}
