package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.LocationEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper implements Mapper<LocationEntity, LocationDto> {
    @Override
    public LocationDto mapToDto(LocationEntity location) {
        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setZoneId(location.getZoneId());
        dto.setX(location.getX());
        dto.setY(location.getY());
        return dto;
    }
}
