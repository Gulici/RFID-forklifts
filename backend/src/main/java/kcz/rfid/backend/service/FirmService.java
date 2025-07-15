package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;

public interface FirmService extends EntityService<FirmEntity> {
    FirmEntity createFirm(FirmRegisterDto firm);
    UserEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto);
    LocationEntity addLocationToFirm(FirmEntity firmEntity, LocationDto locationDto);
    DeviceEntity addDeviceToFirm(FirmEntity firmEntity, DeviceDto deviceDto);
}
