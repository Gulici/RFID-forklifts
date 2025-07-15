package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class RegisterDeviceDto {
    private String username;
    private String password;
    private String deviceName;
    private String publicKey;
}
