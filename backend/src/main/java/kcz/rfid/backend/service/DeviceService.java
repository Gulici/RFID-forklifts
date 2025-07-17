package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;

import java.util.Collection;
import java.util.UUID;

public interface DeviceService extends EntityService<DeviceEntity> {
    DeviceEntity createDevice(DeviceRegisterDto deviceDto, FirmEntity firm);
    DeviceEntity updateDevice(DeviceDto deviceDto, UUID forkliftId);
    void updateLocation(DeviceEntity device, LocationEntity locationEntity);

    Collection<DeviceEntity> findDevicesByFirm(FirmEntity firm);

    DeviceEntity findDeviceByFingerprint(String publicKey);
}
