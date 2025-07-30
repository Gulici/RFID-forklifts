package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO containing JWT token")
public class JwtDto {
    @Schema(description = "JWT token string", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String jwt;

    public JwtDto(String jwt) {
        this.jwt = jwt;
    }
}
