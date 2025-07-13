package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.ForkliftRepository;
import kcz.rfid.backend.service.ForkliftService;
import kcz.rfid.backend.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ForkliftServiceImpl extends EntityServiceBase<ForkliftEntity> implements ForkliftService {

    private final ForkliftRepository forkliftRepository;
    private final LocationService locationService;

    public ForkliftServiceImpl(EntityRepository<ForkliftEntity> repository, ForkliftRepository forkliftRepository, LocationService locationService) {
        super(repository);
        this.forkliftRepository = forkliftRepository;
        this.locationService = locationService;
    }

    @Override
    public ForkliftEntity createForklift(ForkliftDto forkliftDto, FirmEntity firmEntity) {
        if (forkliftDto.getName() == null || forkliftDto.getName().isEmpty()) {
            throw new IllegalArgumentException("ForkliftDto cannot have null or empty values");
        }

        if (forkliftRepository.findByName(forkliftDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Forklift with name " + forkliftDto.getName() + " already exists");
        }
        ForkliftEntity forkliftEntity = new ForkliftEntity();
        forkliftEntity.setName(forkliftDto.getName());
        forkliftEntity.setFirm(firmEntity);

        return forkliftRepository.save(forkliftEntity);
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
        var historyEntry = locationService.createNewLocationHistoryEntry(locationEntity, forkliftEntity);
        forkliftEntity.getLocationHistoryList().add(historyEntry);
        forkliftRepository.save(forkliftEntity);
    }
}
