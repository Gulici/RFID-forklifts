package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.FirmRepository;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.ForkliftService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FirmServiceImpl extends EntityServiceBase<FirmEntity> implements FirmService {

    private final FirmRepository firmRepository;
    private final UserService userService;
    private final LocationService locationService;
    private final ForkliftService forkliftService;

    public FirmServiceImpl(EntityRepository<FirmEntity> repository, FirmRepository firmRepository, UserService userService, LocationService locationService, ForkliftService forkliftService) {
        super(repository);
        this.firmRepository = firmRepository;
        this.userService = userService;
        this.locationService = locationService;
        this.forkliftService = forkliftService;
    }

    @Override
    public FirmEntity createFirm(FirmDto firm) {
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
    public ForkliftEntity addForkliftToFirm(FirmEntity firmEntity, ForkliftDto forkliftDto) {
        ForkliftEntity newForklift = forkliftService.createForklift(forkliftDto, firmEntity);
        firmEntity.getForklifts().add(newForklift);
        firmRepository.save(firmEntity);
        return newForklift;
    }
}
