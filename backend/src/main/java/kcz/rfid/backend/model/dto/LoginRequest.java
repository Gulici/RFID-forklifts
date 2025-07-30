package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for user login")
public class LoginRequest {
    @Schema(description = "Username for login", example = "user123")
    private String username;
    @Schema(description = "Password for login", example = "P@ssw0rd!")
    private String password;
}
