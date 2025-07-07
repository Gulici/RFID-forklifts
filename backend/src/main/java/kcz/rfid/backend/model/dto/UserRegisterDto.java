package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class UserRegisterDto {
    private String username;
    private String email;
    private String firmName;
    private String password;
}
