package kcz.rfid.backend.service;

import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.*;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class InitService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final ForkliftRepository forkliftRepository;
    private final FirmRepository firmRepository;
    private final FirmService firmService;

    public InitService(RoleRepository roleRepository, UserRepository userRepository, LocationRepository locationRepository, ForkliftRepository forkliftRepository, FirmRepository firmRepository, FirmService firmService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.forkliftRepository = forkliftRepository;
        this.firmRepository = firmRepository;
        this.firmService = firmService;
    }

    public void createRoles() {
        if (roleRepository.findAll().isEmpty()) {
            RoleEntity admin = new RoleEntity();
            admin.setName(RoleEnum.ROLE_ADMIN);
            roleRepository.save(admin);

            RoleEntity user = new RoleEntity();
            user.setName(RoleEnum.ROLE_USER);
            roleRepository.save(user);
        }
    }

    public void createUsers() {
        if (userRepository.findAll().isEmpty()) {
            RoleEntity adminRole = roleRepository.findRoleEntityByName(RoleEnum.ROLE_ADMIN).orElseThrow();
            RoleEntity userRole = roleRepository.findRoleEntityByName(RoleEnum.ROLE_USER).orElseThrow();
            FirmEntity firm = firmRepository.getFirmEntityByFirmName("Test Firm");

            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@mail.com");
            HashSet<RoleEntity> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            admin.setFirm(firm);

            UserEntity user = new UserEntity();
            user.setUsername("user");
            user.setPassword("password");
            user.setEmail("user@mail.com");
            HashSet<RoleEntity> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);
            user.setFirm(firm);

            firm.getUsers().add(admin);
            firm.getUsers().add(user);

            firmRepository.save(firm);
        }
    }

    public void createFirmAndForklifts() {
        if (firmRepository.findAll().isEmpty()) {
            FirmEntity firm = new FirmEntity();
            firm.setFirmName("Test Firm");
//            firm = firmRepository.save(firm);

            LocationEntity location = new LocationEntity();
            location.setFirm(firm);
            location.setX(0);
            location.setY(0);
            location.setName("LOCATION_1");

            LocationEntity location2 = new LocationEntity();
            location2.setFirm(firm);
            location2.setX(10);
            location2.setY(10);
            location2.setName("LOCATION_2");

            firm.getLocations().add(location);
            firm.getLocations().add(location2);

            ForkliftEntity forklift = new ForkliftEntity();
            forklift.setFirm(firm);
            forklift.setName("FORKLIFT_1");
            forklift.setLocation(location);

            ForkliftEntity forklift2 = new ForkliftEntity();
            forklift2.setFirm(firm);
            forklift2.setName("FORKLIFT_2");
            forklift2.setLocation(location);

            List<ForkliftEntity> forklifts = new ArrayList<>();
            forklifts.add(forklift);
            forklifts.add(forklift2);
            firm.setForklifts(forklifts);

            firmRepository.save(firm);
        }
    }

    public void testFirmCreation() {
        if (firmRepository.findAll().isEmpty()) {
            FirmDto firmDto = new FirmDto();
            firmDto.setFirmName("Test Firm");
            firmDto.setAdminName("admin");
            firmDto.setAdminEmail("admin@mail.com");
            firmDto.setPassword("password");

            FirmEntity firm = firmService.createFirm(firmDto);

            UserRegisterDto userRegisterDto = new UserRegisterDto();
            userRegisterDto.setFirmName("Test Firm");
            userRegisterDto.setUsername("User");
            userRegisterDto.setPassword("password");
            userRegisterDto.setEmail("User@mail.com");
            firmService.addUserToFirm(firm, userRegisterDto);
        }
    }

}
