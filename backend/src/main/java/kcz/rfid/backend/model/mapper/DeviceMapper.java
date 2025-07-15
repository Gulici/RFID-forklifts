package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.entity.DeviceEntity;
import org.springframework.stereotype.Component;

@Component
public class DeviceMapper implements Mapper<DeviceEntity, DeviceDto> {

    private final LocationMapper locationMapper;

    public DeviceMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    @Override
    public DeviceDto mapToDto(DeviceEntity device) {
        DeviceDto dto = new DeviceDto();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setLocation(locationMapper.mapToDto(device.getLocation()));
        return dto;
    }
}
