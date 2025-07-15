package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import org.springframework.stereotype.Component;

@Component
public class FirmMapper implements Mapper<FirmEntity, FirmDto> {

    private final LocationMapper locationMapper;
    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;

    public FirmMapper(LocationMapper locationMapper, DeviceMapper deviceMapper, UserMapper userMapper) {
        this.locationMapper = locationMapper;
        this.deviceMapper = deviceMapper;
        this.userMapper = userMapper;
    }

    @Override
    public FirmDto mapToDto(FirmEntity firmEntity) {
        FirmDto firmDto = new FirmDto();
        firmDto.setId(firmEntity.getId());
        firmDto.setFirmName(firmEntity.getFirmName());
        firmDto.setUsers(firmEntity.getUsers().stream().map(userMapper::mapToDto).toList());
        firmDto.setDevices(firmEntity.getDevices().stream().map(deviceMapper::mapToDto).toList());
        firmDto.setLocations(firmEntity.getLocations().stream().map(locationMapper::mapToDto).toList());
        return firmDto;
    }
}
