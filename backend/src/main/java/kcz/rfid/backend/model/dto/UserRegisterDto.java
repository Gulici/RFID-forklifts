package kcz.rfid.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterDto {
    @NotBlank
    @Length(min = 5, max = 255)
    private String username;
    @NotBlank
    @Length(min = 5, max = 255)
    private String email;
    @NotBlank
    @Length(min = 5, max = 255)
    private String firmName;
    @NotBlank
    @Length(min = 8, max = 255)
    private String password;
}
