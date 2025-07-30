package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "DTO representing a location with coordinates")
public class LocationDto {
    @Schema(description = "Unique identifier of the location", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "Name of the location", example = "Warehouse Zone A")
    private String name;
    @Schema(description = "Zone identifier", example = "1")
    private Integer zoneId;
    @Schema(description = "X coordinate within the zone", example = "100")
    private int x;
    @Schema(description = "Y coordinate within the zone", example = "200")
    private int y;
}
