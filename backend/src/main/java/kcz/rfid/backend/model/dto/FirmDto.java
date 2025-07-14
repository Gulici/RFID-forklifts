package kcz.rfid.backend.model.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class FirmDto {
    private UUID id;
    private String firmName;
    private List<UserDto> users;
    private List<LocationDto> locations;
    private List<ForkliftDto> forklifts;
}
