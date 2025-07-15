package kcz.rfid.backend.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DeviceLocationDto {
    private UUID id;
    private UUID firmId;
    private String epcCode;
}
