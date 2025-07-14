package kcz.rfid.backend.model.mapper;

import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import org.springframework.stereotype.Component;

@Component
public class ForkliftMapper implements Mapper<ForkliftEntity, ForkliftDto> {

    private final LocationMapper locationMapper;

    public ForkliftMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    @Override
    public ForkliftDto mapToDto(ForkliftEntity forklift) {
        ForkliftDto dto = new ForkliftDto();
        dto.setId(forklift.getId());
        dto.setName(forklift.getName());
        dto.setLocation(locationMapper.mapToDto(forklift.getLocation()));
        return dto;
    }
}
