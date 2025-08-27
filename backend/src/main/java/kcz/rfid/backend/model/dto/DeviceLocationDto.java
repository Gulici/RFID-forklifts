package kcz.rfid.backend.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.UUID;

@Data
@Schema(description = "Basic device location information used to update location and log isAlive data")
public class DeviceLocationDto {
    @Schema(description = "Unique identifier of the device location", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
    private UUID id;
    @Schema(description = "Firm identifier associated with the device", example = "1b9d6bcd-bbfd-4b2d-9b5d-ab8dfbbd4bed")
    private UUID firmId;
    @Schema(description = "EPC code of the device", example = "AC3D0141")
    private String epcCode;
}
