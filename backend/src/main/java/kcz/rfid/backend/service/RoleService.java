package kcz.rfid.backend.service;

import kcz.rfid.backend.model.entity.RoleEntity;

public interface RoleService extends EntityService<RoleEntity> {
    RoleEntity getRoleByName(String roleName);
}
