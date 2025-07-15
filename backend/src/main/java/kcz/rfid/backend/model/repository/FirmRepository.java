package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FirmRepository extends EntityRepository<FirmEntity>{
    FirmEntity getFirmEntityByFirmName(String firmName);

    Optional<FirmEntity> findByFirmName(String firmName);

    FirmEntity findFirmEntityById(UUID id);
}
