package kcz.rfid.backend;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.ForkliftDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.ForkliftEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.FirmRepository;
import kcz.rfid.backend.model.repository.LocationRepository;
import kcz.rfid.backend.model.repository.UserRepository;
import kcz.rfid.backend.service.FirmService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class FirmServiceTests {

    @Autowired
    private FirmService firmService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FirmRepository firmRepository;
    @Autowired
    private LocationRepository locationRepository;


    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
        firmRepository.deleteAll();
    }

    @Test
    void shouldCreateNewFirm() {
        int firmsNum = firmRepository.findAll().size();
        FirmDto firmDto = new FirmDto();
        firmDto.setFirmName("Firm From Test");
        firmDto.setAdminName("Admin From Test");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");

        FirmEntity newFirm = firmService.createFirm(firmDto);

        int newFirmsNum = firmRepository.findAll().size();
        UserEntity adminFromRepo = userRepository.findByEmail(firmDto.getAdminEmail()).orElse(null);
        UserEntity admin = newFirm.getUsers().get(0);

        Assertions.assertEquals(firmsNum + 1, newFirmsNum);
        Assertions.assertEquals(firmDto.getFirmName(), newFirm.getFirmName());
        Assertions.assertNotNull(adminFromRepo);
        Assertions.assertEquals(admin, adminFromRepo);
    }

    @Test
    void shouldNotCreateNewFirm1() {
        FirmDto firmDto = new FirmDto();
        firmDto.setFirmName("Firm From Test");
        firmDto.setAdminName("Admin From Test");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");

        firmService.createFirm(firmDto);

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmDto));
    }

    @Test
    void shouldNotCreateNewFirm2() {
        FirmDto firmDto = new FirmDto();
        firmDto.setFirmName("Firm From Test");
        firmDto.setAdminName("Admin From Test");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");

        firmService.createFirm(firmDto);

        firmDto.setFirmName("Firm From Test");
        firmDto.setAdminName("Diffrent Admin From Test");
        firmDto.setAdminEmail("admin2@firmtest.com");
        firmDto.setPassword("password");

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmDto));
    }

    @Test
    void shouldNotCreateNewFirm3() {
        FirmDto firmDto = new FirmDto();
        firmDto.setFirmName("Firm From Test");
        firmDto.setAdminName("Admin From Test");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");
        firmService.createFirm(firmDto);

        firmDto.setFirmName("Firm From Test 2");
        firmDto.setAdminName("Admin From Test");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmDto));
    }

    @Test
    void shouldNotCreateNewFirm4() {
        FirmDto firmDto = new FirmDto();
        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.createFirm(firmDto));
    }

    @Test
    void shouldAddUserToFirm() {
        FirmEntity firm = this.createFirm();

        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setFirmName("Firm");
        userRegisterDto.setUsername("User");
        userRegisterDto.setPassword("password");
        userRegisterDto.setEmail("user@firmtest.com");

        UserEntity user =  firmService.addUserToFirm(firm, userRegisterDto);

        Assertions.assertNotNull(user);
        Assertions.assertEquals("User", user.getUsername());
        Assertions.assertEquals("user@firmtest.com", user.getEmail());
        Assertions.assertNotEquals("password", user.getPassword());
        Assertions.assertEquals(firm, user.getFirm());
        Assertions.assertFalse(user.getRoles().isEmpty());
        Assertions.assertTrue(user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleEnum.ROLE_USER)));
        Assertions.assertFalse(user.getRoles().stream().anyMatch(role -> role.getName().equals(RoleEnum.ROLE_ADMIN)));
    }

    @Test
    void shouldNotAddUserToFirm1() {
        FirmEntity firm = this.createFirm();

        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setFirmName("Firm");
        userRegisterDto.setUsername("User");
        userRegisterDto.setPassword("password");
        userRegisterDto.setEmail("admin@firmtest.com");

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.addUserToFirm(firm, userRegisterDto));
    }

    @Test
    void shouldNotAddUserToFirm2() {
        FirmEntity firm = this.createFirm();

        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setFirmName("Firm");
        userRegisterDto.setUsername("User");
        userRegisterDto.setPassword("password");

        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.addUserToFirm(firm, userRegisterDto));
    }

    @Test
    void shouldAddLocationToFirm() {
        FirmEntity firm = this.createFirm();

        LocationDto locationDto = new LocationDto();
        locationDto.setName("Location");
        locationDto.setZoneId(10);
        locationDto.setX(0);
        locationDto.setY(0);

        LocationEntity location =  firmService.addLocationToFirm(firm, locationDto);

        Assertions.assertNotNull(location);
        Assertions.assertEquals("Location", location.getName());
        Assertions.assertEquals(10, location.getZoneId());
        Assertions.assertEquals(0, location.getX());
        Assertions.assertEquals(0, location.getY());
        Assertions.assertEquals(firm, location.getFirm());

        List<LocationEntity> locations = locationRepository.findAllByFirmId(firm.getId());
        Assertions.assertEquals(1, locations.size());
        Assertions.assertEquals(location ,locations.get(0));
    }

    @Test
    void shouldNotAddLocationToFirm() {
        FirmEntity firm = this.createFirm();

        LocationDto locationDto = new LocationDto();

        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.addLocationToFirm(firm, locationDto));
    }

    @Test
    void shouldNotAddLocationToFirm1() {
        FirmEntity firm = this.createFirm();

        LocationDto locationDto = new LocationDto();
        locationDto.setName("Location");
        locationDto.setZoneId(10);
        locationDto.setX(0);
        locationDto.setY(0);
        firmService.addLocationToFirm(firm, locationDto);

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.addLocationToFirm(firm, locationDto));
    }

    @Test
    void shouldAddForkliftToFirm() {
        FirmEntity firm = this.createFirm();

        ForkliftDto forkliftDto = new ForkliftDto();
        forkliftDto.setName("Forklift");

        ForkliftEntity forklift =  firmService.addForkliftToFirm(firm, forkliftDto);

        Assertions.assertNotNull(forklift);
        Assertions.assertEquals("Forklift", forklift.getName());
        Assertions.assertEquals(firm, forklift.getFirm());
    }

    @Test
    void shouldNotAddForkliftToFirm() {
        FirmEntity firm = this.createFirm();
        ForkliftDto forkliftDto = new ForkliftDto();
        forkliftDto.setName("Forklift");
        firmService.addForkliftToFirm(firm, forkliftDto);

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.addForkliftToFirm(firm, forkliftDto));
    }

    @Test
    void shouldNotAddForkliftToFirm1() {
        FirmEntity firm = this.createFirm();
        ForkliftDto forkliftDto = new ForkliftDto();
        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.addForkliftToFirm(firm, forkliftDto));
    }

    private FirmEntity createFirm() {
        FirmDto firmDto = new FirmDto();
        firmDto.setFirmName("Firm");
        firmDto.setAdminName("Admin");
        firmDto.setAdminEmail("admin@firmtest.com");
        firmDto.setPassword("password");
        return firmService.createFirm(firmDto);
    }
}
