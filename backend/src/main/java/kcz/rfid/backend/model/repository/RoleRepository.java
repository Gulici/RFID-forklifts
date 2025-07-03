package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;

import java.util.Optional;

public interface RoleRepository extends EntityRepository<RoleEntity> {
    Optional<RoleEntity> findRoleEntityByName(RoleEnum name);
}
