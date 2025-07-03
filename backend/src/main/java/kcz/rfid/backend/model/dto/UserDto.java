package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class UserDto {
    private String username;
    private String email;
    private String password;
}
