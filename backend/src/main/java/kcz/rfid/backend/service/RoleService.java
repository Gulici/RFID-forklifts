package kcz.rfid.backend.service;

import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;

public interface RoleService extends EntityService<RoleEntity> {
    RoleEntity getRoleByName(String roleName);

    RoleEntity getRoleByRole(RoleEnum role);
}
