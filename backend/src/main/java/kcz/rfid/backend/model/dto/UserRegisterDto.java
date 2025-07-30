package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "Request DTO for user registration")
public class UserRegisterDto {
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "Username", example = "john_doe")
    private String username;
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "Name of the firm the user belongs to", example = "Example Corp")
    private String firmName;
    @NotBlank
    @Length(min = 8, max = 255)
    @Schema(description = "Password for the new user account", example = "StrongP@ssw0rd!")
    private String password;
}
