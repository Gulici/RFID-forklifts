package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Schema(description = "DTO representing the history record of a device location at a specific location")
public class LocationHistoryDto {
    @Schema(description = "Unique identifier of the location history record", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "Device information")
    private DeviceDto deviceDto;
    @Schema(description = "Location information")
    private LocationDto locationDto;
    @Schema(description = "Timestamp of the location record", example = "2025-07-30T12:34:56")
    private Instant timestamp;
}
