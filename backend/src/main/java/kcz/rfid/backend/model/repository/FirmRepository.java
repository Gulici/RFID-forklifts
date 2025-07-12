package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.FirmEntity;

import java.util.Optional;

public interface FirmRepository extends EntityRepository<FirmEntity>{
    FirmEntity getFirmEntityByFirmName(String firmName);

    Optional<FirmEntity> findByFirmName(String firmName);
}
