package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.RegisterDeviceDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.FirmRepository;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FirmServiceImpl extends EntityServiceBase<FirmEntity> implements FirmService {

    private final FirmRepository firmRepository;
    private final UserService userService;
    private final LocationService locationService;
    private final DeviceService deviceService;

    public FirmServiceImpl(EntityRepository<FirmEntity> repository, FirmRepository firmRepository, UserService userService, LocationService locationService, DeviceService deviceService) {
        super(repository);
        this.firmRepository = firmRepository;
        this.userService = userService;
        this.locationService = locationService;
        this.deviceService = deviceService;
    }

    @Override
    public FirmEntity createFirm(FirmRegisterDto firm) {
        if (firm.getFirmName() == null || firm.getFirmName().isEmpty()
            || firm.getAdminName() == null || firm.getAdminName().isEmpty()
            || firm.getAdminEmail() == null || firm.getAdminEmail().isEmpty()
            || firm.getPassword() == null || firm.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Firm must have firmName and adminName and adminEmail and password");
        }
        if (firmRepository.findByFirmName(firm.getFirmName()).isPresent()) {
            throw new ResourceAlreadyExistsException("Firm with name " + firm.getFirmName() + " already exists");
        }

        FirmEntity firmEntity = new FirmEntity();
        firmEntity.setFirmName(firm.getFirmName());

        firmEntity = firmRepository.save(firmEntity);

        UserRegisterDto adminDto = new UserRegisterDto();
        adminDto.setFirmName(firm.getFirmName());
        adminDto.setEmail(firm.getAdminEmail());
        adminDto.setUsername(firm.getAdminName());
        adminDto.setPassword(firm.getPassword());

        UserEntity admin = userService.createAdmin(adminDto, firmEntity);
        firmEntity.getUsers().add(admin);
        return firmEntity;
    }

    @Override
    public UserEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto) {
        UserEntity newUser = userService.createUser(userRegisterDto, firmEntity);
        firmRepository.save(firmEntity);
        return newUser;
    }

    @Override
    public LocationEntity addLocationToFirm(FirmEntity firmEntity, LocationDto locationDto) {
        LocationEntity newLocation = locationService.createLocation(locationDto, firmEntity);
        firmEntity.getLocations().add(newLocation);
        firmRepository.save(firmEntity);
        return newLocation;
    }

    @Override
    public DeviceEntity addDeviceToFirm(FirmEntity firmEntity, RegisterDeviceDto deviceDto) {
        DeviceEntity device = deviceService.createDevice(deviceDto, firmEntity);
        firmEntity.getDevices().add(device);
        firmRepository.save(firmEntity);
        return device;
    }
}
