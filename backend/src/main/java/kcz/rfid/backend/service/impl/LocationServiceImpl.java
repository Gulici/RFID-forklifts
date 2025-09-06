package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.LocationHistoryRepository;
import kcz.rfid.backend.model.repository.LocationRepository;
import kcz.rfid.backend.service.LocationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocationServiceImpl extends EntityServiceBase<LocationEntity> implements LocationService {

    private final LocationRepository locationRepository;
    private final LocationHistoryRepository locationHistoryRepository;

    public LocationServiceImpl(EntityRepository<LocationEntity> repository, LocationRepository locationRepository, LocationHistoryRepository locationHistoryRepository) {
        super(repository);
        this.locationRepository = locationRepository;
        this.locationHistoryRepository = locationHistoryRepository;
    }

    @Override
    public LocationEntity createLocation(LocationDto locationDto, FirmEntity firmEntity) {
        if (locationDto.getZoneId() == null || locationDto.getName() == null || locationDto.getName().isEmpty()) {
            throw new IllegalArgumentException("LocationDto cannot have null or empty values");
        }

        locationRepository.findByName(locationDto.getName()).ifPresent(location -> {
            if (location.getFirm().equals(firmEntity)) {
                throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
            }
        });
        locationRepository.findByZoneId(locationDto.getZoneId()).ifPresent(location -> {
            if (location.getFirm().equals(firmEntity)) {
                throw new ResourceAlreadyExistsException("Location with Zone ID " + locationDto.getZoneId() + " already exists");
            }
        });

        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(locationDto.getName());
        locationEntity.setZoneId(locationDto.getZoneId());
        locationEntity.setFirm(firmEntity);
        locationEntity.setX(locationDto.getX());
        locationEntity.setY(locationDto.getY());

        return locationRepository.save(locationEntity);
    }

    @Override
    public LocationEntity updateLocation(LocationDto locationDto, UUID locationId) {
        LocationEntity locationEntity = locationRepository.findById(locationId).orElse(null);
        if (locationEntity == null) {
            throw new ResourceNotFoundException("Location with id " + locationId + " not found");
        }

        if (locationDto.getName() != null) {
            locationRepository.findByName(locationDto.getName()).ifPresent(location -> {
                if (location.getFirm().equals(locationEntity.getFirm())) {
                    throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
                }
            });
            locationEntity.setName(locationDto.getName());
        }
        if (locationDto.getZoneId() != null) {
            locationRepository.findByZoneId(locationDto.getZoneId()).ifPresent(location -> {
                if (location.getFirm().equals(locationEntity.getFirm())) {
                    throw new ResourceAlreadyExistsException("Location with Zone ID " + locationDto.getZoneId() + " already exists");
                }
            });
            locationEntity.setZoneId(locationDto.getZoneId());
        }

        locationEntity.setX(locationDto.getX());
        locationEntity.setY(locationDto.getY());

        return locationRepository.save(locationEntity);
    }

    @Override
    public LocationHistoryEntity createNewLocationHistoryEntry(LocationEntity location, DeviceEntity device) {
        LocationHistoryEntity locationHistoryEntity = new LocationHistoryEntity();
        locationHistoryEntity.setLocation(location);
        locationHistoryEntity.setDevice(device);

        Instant now = Instant.now();
        locationHistoryEntity.setTimestamp(now);
        device.setTimestamp(now);

        return locationHistoryRepository.save(locationHistoryEntity);
    }

    @Override
    public List<LocationEntity> getLocationsByFirm(FirmEntity firmEntity) {
        return locationRepository.findAllByFirm(firmEntity);
    }

    @Override
    public List<LocationHistoryEntity> getLocationHistoryForFirm(FirmEntity firm) {
        return locationHistoryRepository.findAllByFirm(firm);
    }

    @Override
    public List<LocationHistoryEntity> getLocationHistoryForForklift(DeviceEntity deviceEntity) {
        return locationHistoryRepository.findAllByDevice(deviceEntity);
    }

    @Override
    public LocationEntity getLocationByFirmAndZoneId(FirmEntity firm, int zoneId) {
        return locationRepository.findByFirmAndZoneId(firm, zoneId);
    }
}
