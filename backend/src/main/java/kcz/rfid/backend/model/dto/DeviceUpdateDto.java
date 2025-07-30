package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO used to update device information")
public class DeviceUpdateDto {
    @Schema(description = "New name of the device", example = "Updated Device Name")
    private String name;
}
