package kcz.rfid.backend.model.dto;

import lombok.Data;

@Data
public class LocationDto {
    private String name;
    private Integer zoneId;
    private int x,y;
}
