package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.LocationRepository;
import kcz.rfid.backend.service.LocationService;

import java.util.UUID;

public class LocationServiceImpl extends EntityServiceBase<LocationEntity> implements LocationService {

    private final LocationRepository locationRepository;

    public LocationServiceImpl(EntityRepository<LocationEntity> repository, LocationRepository locationRepository) {
        super(repository);
        this.locationRepository = locationRepository;
    }

    @Override
    public LocationEntity createLocation(LocationDto locationDto, FirmEntity firmEntity) {
        if (locationRepository.findByName(locationDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
        }
        if (locationRepository.findByZoneId(locationDto.getZoneId())) {
            throw new ResourceAlreadyExistsException("Zone with id " + locationDto.getZoneId() + " already exists");
        }

        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setName(locationDto.getName());
        locationEntity.setZoneId(locationDto.getZoneId());
        locationEntity.setFirm(firmEntity);
        locationEntity.setX(locationDto.getX());
        locationEntity.setY(locationDto.getY());

        return locationEntity;
    }

    @Override
    public LocationEntity updateLocation(LocationDto locationDto, UUID locationId) {
        if (locationDto.getName() != null && locationRepository.findByName(locationDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Location with name " + locationDto.getName() + " already exists");
        }
        if (locationDto.getZoneId() != null && locationRepository.findByZoneId(locationDto.getZoneId())) {
            throw new ResourceAlreadyExistsException("Zone with id " + locationDto.getZoneId() + " already exists");
        }

        LocationEntity locationEntity = locationRepository.findById(locationId).orElse(null);
        if (locationEntity == null) {
            throw new ResourceNotFoundException("Location with id " + locationId + " not found");
        }

        locationEntity.setName(locationDto.getName());
        locationEntity.setZoneId(locationDto.getZoneId());
        locationEntity.setX(locationDto.getX());
        locationEntity.setY(locationDto.getY());
        return locationEntity;
    }
}
