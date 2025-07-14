package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.LocationHistoryDto;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationHistoryMapper implements Mapper<LocationHistoryEntity, LocationHistoryDto> {

    private final LocationMapper locationMapper;
    private final ForkliftMapper forkliftMapper;

    public LocationHistoryMapper(LocationMapper locationMapper, ForkliftMapper forkliftMapper) {
        this.locationMapper = locationMapper;
        this.forkliftMapper = forkliftMapper;
    }

    @Override
    public LocationHistoryDto mapToDto(LocationHistoryEntity locationHistoryEntity) {
        LocationHistoryDto dto = new LocationHistoryDto();
        dto.setId(locationHistoryEntity.getId());
        dto.setLocationDto(locationMapper.mapToDto(locationHistoryEntity.getLocation()));
        dto.setForkliftDto(forkliftMapper.mapToDto(locationHistoryEntity.getForklift()));
        dto.setTimestamp(locationHistoryEntity.getTimestamp());
        return dto;
    }
}
