package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.LocationHistoryDto;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import org.springframework.stereotype.Component;

@Component
public class LocationHistoryMapper implements Mapper<LocationHistoryEntity, LocationHistoryDto> {

    private final LocationMapper locationMapper;
    private final DeviceMapper deviceMapper;

    public LocationHistoryMapper(LocationMapper locationMapper, DeviceMapper deviceMapper) {
        this.locationMapper = locationMapper;
        this.deviceMapper = deviceMapper;
    }

    @Override
    public LocationHistoryDto mapToDto(LocationHistoryEntity locationHistoryEntity) {
        LocationHistoryDto dto = new LocationHistoryDto();
        dto.setId(locationHistoryEntity.getId());
        dto.setLocationDto(locationMapper.mapToDto(locationHistoryEntity.getLocation()));
        dto.setDeviceDto(deviceMapper.mapToDto(locationHistoryEntity.getDevice()));
        dto.setTimestamp(locationHistoryEntity.getTimestamp());
        return dto;
    }
}
