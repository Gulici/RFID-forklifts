package kcz.rfid.backend.model.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class LocationDto {
    private UUID id;
    private String name;
    private Integer zoneId;
    private int x,y;
}
