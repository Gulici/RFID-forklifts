package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class DeviceRegisterDto {
    private String username;
    private String password;
    private String deviceName;
    private String publicKey;
}
