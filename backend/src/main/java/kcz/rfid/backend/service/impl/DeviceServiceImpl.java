package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.DeviceRepository;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.utils.PemUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Slf4j
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
    public DeviceEntity createDevice(DeviceRegisterDto deviceDto, FirmEntity firmEntity) {

        if (deviceRepository.findByFingerprint(PemUtils.computeFingerprint(deviceDto.getPublicKey())).isPresent()) {
            throw new ResourceAlreadyExistsException("Device already exists");
        }
        if (deviceRepository.findByName(deviceDto.getDeviceName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Device with name " + deviceDto.getDeviceName() + " already exists");
        }

        DeviceEntity device = new DeviceEntity();
        device.setFingerprint(PemUtils.computeFingerprint(deviceDto.getPublicKey()));
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
        device.setLastSeen(Instant.now());

        LocationEntity currentLocation = device.getLocation();
        if (currentLocation == null || !currentLocation.equals(locationEntity)) {
            device.setLocation(locationEntity);
            var historyEntry = locationService.createNewLocationHistoryEntry(locationEntity, device);
            device.getLocationHistoryList().add(historyEntry);
            log.info("Location updated for device {} firm: {} location: {}",
                    device.getName(), device.getFirm().getFirmName(), locationEntity.getName());
        }

        deviceRepository.save(device);
    }


    @Override
    public Collection<DeviceEntity> findDevicesByFirm(FirmEntity firm) {
        return deviceRepository.findDeviceEntitiesByFirm(firm);
    }

    @Override
    public DeviceEntity findDeviceByFingerprint(String fingerprint) {
        return deviceRepository.findByFingerprint(fingerprint).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid public key")
        );
    }
}
