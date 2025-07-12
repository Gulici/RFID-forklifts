package kcz.rfid.backend.service.impl;

import kcz.rfid.backend.exeption.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.repository.EntityRepository;
import kcz.rfid.backend.model.repository.FirmRepository;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class FirmServiceImpl extends EntityServiceBase<FirmEntity> implements FirmService {

    private final FirmRepository firmRepository;
    private final UserService userService;

    public FirmServiceImpl(EntityRepository<FirmEntity> repository, FirmRepository firmRepository, UserService userService) {
        super(repository);
        this.firmRepository = firmRepository;
        this.userService = userService;
    }

    @Override
    public FirmEntity createFirm(FirmDto firm) {
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
    public FirmEntity addUserToFirm(FirmEntity firmEntity, UserRegisterDto userRegisterDto) {
        userService.createUser(userRegisterDto, firmEntity);
        return firmRepository.save(firmEntity);
    }

}
