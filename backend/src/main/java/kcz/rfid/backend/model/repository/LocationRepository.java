package kcz.rfid.backend.model.repository;

import kcz.rfid.backend.model.entity.LocationEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends EntityRepository<LocationEntity> {
    Optional<LocationEntity> findByName(String name);
    Optional<LocationEntity> findByZoneId(int zoneId);
    List<LocationEntity> findAllByFirmId(UUID firm_id);
}
