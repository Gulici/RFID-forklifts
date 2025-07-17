package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.*;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;

public interface FirmService extends EntityService<FirmEntity> {
    FirmEntity createFirm(FirmRegisterDto firm);
    UserEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto);
    LocationEntity addLocationToFirm(FirmEntity firmEntity, LocationDto locationDto);
    DeviceEntity addDeviceToFirm(FirmEntity firmEntity, DeviceRegisterDto deviceDto);

    FirmEntity updateFirm(FirmEntity firm, FirmDto dto);

    void updateDeviceLocation(DeviceLocationDto dto);
}
