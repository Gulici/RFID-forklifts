package kcz.rfid.backend.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class DeviceDto {
    private UUID id;
    private String name;
    private LocationDto location;
}
