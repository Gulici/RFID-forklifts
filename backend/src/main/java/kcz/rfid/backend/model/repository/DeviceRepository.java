package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;

import java.util.Collection;
import java.util.Optional;

public interface DeviceRepository extends EntityRepository<DeviceEntity> {
    Optional<DeviceEntity> findByName(String name);
    Collection<DeviceEntity> findDeviceEntitiesByFirm(FirmEntity firm);
}
