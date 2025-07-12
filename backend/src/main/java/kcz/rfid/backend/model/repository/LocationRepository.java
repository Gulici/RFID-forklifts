package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.LocationEntity;

import java.util.Optional;

public interface LocationRepository extends EntityRepository<LocationEntity> {
    Optional<LocationEntity> findByName(String name);

    boolean findByZoneId(int zoneId);
}
