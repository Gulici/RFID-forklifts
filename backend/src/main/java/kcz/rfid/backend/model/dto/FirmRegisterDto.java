package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Schema(description = "DTO used for registering a new firm with its admin creation")
public class FirmRegisterDto {
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "Name of the firm", example = "Example Corp")
    private String firmName;
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "Administrator username", example = "adminUser")
    private String adminName;
    @NotBlank
    @Length(min = 5, max = 255)
    @Schema(description = "Administrator email", example = "admin@example.com")
    private String adminEmail;
    @NotBlank
    @Length(min = 8, max = 255)
    @Schema(description = "Administrator password", example = "StrongP@ssword123")
    private String password;
}
