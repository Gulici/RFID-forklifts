package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class FirmDto {
    private String firmName;
    private String adminName;
    private String adminEmail;
    private String password;
}
