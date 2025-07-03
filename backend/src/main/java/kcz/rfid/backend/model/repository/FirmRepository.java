package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.FirmEntity;

public interface FirmRepository extends EntityRepository<FirmEntity>{
    FirmEntity getFirmEntityByFirmName(String firmName);
}
