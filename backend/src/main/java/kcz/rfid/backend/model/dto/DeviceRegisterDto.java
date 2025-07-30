package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO used for registering a new device")
public class DeviceRegisterDto {
    @Schema(description = "Username of the firm admin", example = "user123")
    private String username;
    @Schema(description = "Password of the firm admin", example = "P@ssw0rd")
    private String password;
    @Schema(description = "Name of the device", example = "RFID Reader 007")
    private String deviceName;
    @Schema(description = "Public key in PEM format associated with the device",  example = "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkq...\n-----END PUBLIC KEY-----")
    private String publicKey;
}
