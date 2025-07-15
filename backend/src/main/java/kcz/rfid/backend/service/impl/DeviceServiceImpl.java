package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.DeviceRepository;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
public class DeviceServiceImpl extends EntityServiceBase<DeviceEntity> implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final LocationService locationService;

    public DeviceServiceImpl(EntityRepository<DeviceEntity> repository, DeviceRepository deviceRepository, LocationService locationService) {
        super(repository);
        this.deviceRepository = deviceRepository;
        this.locationService = locationService;
    }

    @Override
    public DeviceEntity createForklift(DeviceDto deviceDto, FirmEntity firmEntity) {
        if (deviceDto.getName() == null || deviceDto.getName().isEmpty()) {
            throw new IllegalArgumentException("DeviceDto cannot have null or empty values");
        }

        if (deviceRepository.findByName(deviceDto.getName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Device with name " + deviceDto.getName() + " already exists");
        }
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setName(deviceDto.getName());
        deviceEntity.setFirm(firmEntity);

        return deviceRepository.save(deviceEntity);
    }

    @Override
    public DeviceEntity updateDevice(DeviceDto deviceDto, UUID forkliftId) {
        DeviceEntity deviceEntity = deviceRepository.findById(forkliftId).orElse(null);
        if (deviceEntity == null) {
            throw new ResourceNotFoundException("Device with id " + forkliftId + " not found");
        }
        deviceEntity.setName(deviceDto.getName());
        return deviceRepository.save(deviceEntity);
    }

    @Override
    public void updateLocation(DeviceEntity device, LocationEntity locationEntity) {
        device.setLocation(locationEntity);
        var historyEntry = locationService.createNewLocationHistoryEntry(locationEntity, device);
        device.getLocationHistoryList().add(historyEntry);
        deviceRepository.save(device);
    }

    @Override
    public Collection<DeviceEntity> findDevicesByFirm(FirmEntity firm) {
        return deviceRepository.findDeviceEntitiesByFirm(firm);
    }
}
