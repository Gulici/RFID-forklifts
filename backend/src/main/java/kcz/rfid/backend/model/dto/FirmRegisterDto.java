package kcz.rfid.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class FirmRegisterDto {
    @NotBlank
    @Length(min = 5, max = 255)
    private String firmName;
    @NotBlank
    @Length(min = 5, max = 255)
    private String adminName;
    @NotBlank
    @Length(min = 5, max = 255)
    private String adminEmail;
    @NotBlank
    @Length(min = 8, max = 255)
    private String password;
}
