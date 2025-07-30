package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "DTO representing a user in the system")
public class UserDto {
    @NotBlank
    @Schema(description = "Unique identifier of the user", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @NotBlank
    @Schema(description = "Username", example = "john_doe")
    private String username;
    @NotBlank
    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;
    @NotBlank
    @Schema(description = "Name of the firm the user belongs to", example = "Example Corp")
    private String firmName;
}
