package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;

public interface FirmService extends EntityService<FirmEntity> {
    FirmEntity createFirm(FirmDto firm);
    FirmEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto);
}
