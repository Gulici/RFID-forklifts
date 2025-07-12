package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.LocationEntity;

import java.util.UUID;

public interface LocationService extends EntityService<LocationEntity> {
    LocationEntity createLocation(LocationDto locationDto, FirmEntity firmEntity);
    LocationEntity updateLocation(LocationDto locationDto, UUID locationId);
}
