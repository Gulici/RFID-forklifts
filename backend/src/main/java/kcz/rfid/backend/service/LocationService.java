package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;

import java.util.List;
import java.util.UUID;

public interface LocationService extends EntityService<LocationEntity> {
    LocationEntity createLocation(LocationDto locationDto, FirmEntity firmEntity);
    LocationEntity updateLocation(LocationDto locationDto, UUID locationId);

    LocationHistoryEntity createNewLocationHistoryEntry(LocationEntity location, DeviceEntity forklift);
    List<LocationEntity> getLocationsByFirm(FirmEntity firmEntity);
    List<LocationHistoryEntity> getLocationHistoryForFirm(FirmEntity firmEntity);
    List<LocationHistoryEntity> getLocationHistoryForForklift(DeviceEntity deviceEntity);


    LocationEntity getLocationByFirmAndZoneId(FirmEntity firm, int zoneId);
}
