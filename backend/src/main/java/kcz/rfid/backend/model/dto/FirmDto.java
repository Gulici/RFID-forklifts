package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Schema(description = "DTO representing a firm with users, locations and devices")
public class FirmDto {
    @Schema(description = "Unique identifier of the firm", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "Name of the firm", example = "Example Corp")
    private String firmName;
    @Schema(description = "List of users associated with the firm")
    private List<UserDto> users;
    @Schema(description = "List of locations associated with the firm")
    private List<LocationDto> locations;
    @Schema(description = "List of devices associated with the firm")
    private List<DeviceDto> devices;
}
