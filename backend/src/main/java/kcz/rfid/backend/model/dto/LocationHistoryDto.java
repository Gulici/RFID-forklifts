package kcz.rfid.backend.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LocationHistoryDto {
    private UUID id;
    private DeviceDto deviceDto;
    private LocationDto locationDto;
    private LocalDateTime timestamp;
}
