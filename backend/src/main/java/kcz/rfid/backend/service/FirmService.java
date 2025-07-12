package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;

public interface FirmService extends EntityService<FirmEntity> {
    FirmEntity createFirm(FirmDto firm);
    UserEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto);
    LocationEntity addLocationToFirm(FirmEntity firmEntity, LocationDto locationDto);
    ForkliftEntity addForkliftToFirm(FirmEntity firmEntity, ForkliftDto forkliftDto);
}
