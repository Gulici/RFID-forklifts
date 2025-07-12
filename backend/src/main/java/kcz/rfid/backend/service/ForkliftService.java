package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;

import java.util.UUID;

public interface ForkliftService extends EntityService<ForkliftEntity> {
    ForkliftEntity createForklift(ForkliftDto forkliftDto, FirmEntity firm);
    ForkliftEntity updateForklift(ForkliftDto forkliftDto, UUID forkliftId);
    void updateLocation(ForkliftEntity forklift, LocationEntity locationEntity);
}
