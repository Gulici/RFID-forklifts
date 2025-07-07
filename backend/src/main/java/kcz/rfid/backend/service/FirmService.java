package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.entity.FirmEntity;

import java.util.UUID;

public interface FirmService extends EntityService<FirmEntity> {
    FirmEntity addFirm(FirmDto firm);
    FirmEntity getFirm(UUID id);
    FirmEntity getFirm(String firmName);
    FirmEntity updateFirm(FirmEntity firm);
    void deleteFirm(UUID id);
}
