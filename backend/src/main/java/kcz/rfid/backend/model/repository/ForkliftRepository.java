package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.ForkliftEntity;

import java.util.Optional;

public interface ForkliftRepository extends EntityRepository<ForkliftEntity> {
    Optional<ForkliftEntity> findByName(String name);
}
