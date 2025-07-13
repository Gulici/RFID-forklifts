package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.LocationHistoryRepository;
import kcz.rfid.backend.model.repository.LocationRepository;
import kcz.rfid.backend.service.LocationService;
import org.springframework.stereotype.Service;

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

        if (locationRepository.findByName(locationDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
        }
        if (locationRepository.findByZoneId(locationDto.getZoneId()).isPresent()) {
            throw new ResourceAlreadyExistsException("Zone with id " + locationDto.getZoneId() + " already exists");
        }

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
            if (locationRepository.findByName(locationDto.getName()).isPresent()) {
                throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
            }
            locationEntity.setName(locationDto.getName());
        }
        if (locationDto.getZoneId() != null) {
            if (locationRepository.findByZoneId(locationDto.getZoneId()).isPresent()) {
                throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
            }
            locationEntity.setName(locationDto.getName());
        }

        locationEntity.setX(locationDto.getX());
        locationEntity.setY(locationDto.getY());

        return locationRepository.save(locationEntity);
    }

    @Override
    public LocationHistoryEntity createNewLocationHistoryEntry(LocationEntity location, ForkliftEntity forklift) {
        LocationHistoryEntity locationHistoryEntity = new LocationHistoryEntity();
        locationHistoryEntity.setLocation(location);
        locationHistoryEntity.setForklift(forklift);
        locationHistoryEntity.setTimestamp(LocalDateTime.now());

        return locationHistoryRepository.save(locationHistoryEntity);
    }

    @Override
    public List<LocationHistoryEntity> getLocationHistoryForFirm(FirmEntity firm) {
        return locationHistoryRepository.findAllByFirm(firm);
    }

    @Override
    public List<LocationHistoryEntity> getLocationHistoryForForklift(ForkliftEntity forkliftEntity) {
        return locationHistoryRepository.findAllByForklift(forkliftEntity);
    }
}
