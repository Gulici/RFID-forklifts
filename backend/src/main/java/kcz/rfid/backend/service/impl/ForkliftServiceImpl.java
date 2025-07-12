package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.ForkliftRepository;
import kcz.rfid.backend.service.ForkliftService;

import java.time.LocalDateTime;
import java.util.UUID;

public class ForkliftServiceImpl extends EntityServiceBase<ForkliftEntity> implements ForkliftService {

    private final ForkliftRepository forkliftRepository;

    public ForkliftServiceImpl(EntityRepository<ForkliftEntity> repository, ForkliftRepository forkliftRepository) {
        super(repository);
        this.forkliftRepository = forkliftRepository;
    }

    @Override
    public ForkliftEntity createForklift(ForkliftDto forkliftDto) {
        if (forkliftRepository.findByName(forkliftDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Forklift with name " + forkliftDto.getName() + " already exists");
        }
        ForkliftEntity forkliftEntity = new ForkliftEntity();
        forkliftEntity.setName(forkliftDto.getName());
        return forkliftEntity;
    }

    @Override
    public ForkliftEntity updateForklift(ForkliftDto forkliftDto, UUID forkliftId) {
        ForkliftEntity forkliftEntity = forkliftRepository.findById(forkliftId).orElse(null);
        if (forkliftEntity == null) {
            throw new ResourceNotFoundException("Forklift with id " + forkliftId + " not found");
        }
        forkliftEntity.setName(forkliftDto.getName());
        return forkliftRepository.save(forkliftEntity);
    }

    @Override
    public void updateLocation(ForkliftEntity forkliftEntity, LocationEntity locationEntity) {
        forkliftEntity.setLocation(locationEntity);
        updateLocationHistory(forkliftEntity, locationEntity);
        forkliftRepository.save(forkliftEntity);
    }

    private void updateLocationHistory(ForkliftEntity forkliftEntity, LocationEntity locationEntity) {
        LocationHistoryEntity newLog = new LocationHistoryEntity();
        newLog.setLocation(locationEntity);
        newLog.setTimestamp(LocalDateTime.now());
        newLog.setForklift(forkliftEntity);
        forkliftEntity.getLocationHistoryList().add(newLog);
    }
}
