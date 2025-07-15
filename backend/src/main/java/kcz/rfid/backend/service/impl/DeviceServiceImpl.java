package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.RegisterDeviceDto;
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
    public DeviceEntity createDevice(RegisterDeviceDto deviceDto, FirmEntity firmEntity) {

        if (deviceRepository.findByPublicKey(deviceDto.getPublicKey()).isPresent()) {
            throw new ResourceAlreadyExistsException("Device already exists");
        }
        if (deviceRepository.findByName(deviceDto.getDeviceName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Device with name " + deviceDto.getDeviceName() + " already exists");
        }

        DeviceEntity device = new DeviceEntity();
        device.setPublicKey(deviceDto.getPublicKey());
        device.setFirm(firmEntity);
        device.setName(deviceDto.getDeviceName());

        return deviceRepository.save(device);
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

    @Override
    public DeviceEntity findDeviceByPublicKey(String publicKey) {
        return deviceRepository.findDeviceEntityByPublicKey(publicKey);
    }
}
