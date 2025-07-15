package kcz.rfid.backend;

import kcz.rfid.backend.exception.ResourceAlreadyExistsException;
import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
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
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm From Test");
        firmRegisterDto.setAdminName("Admin From Test");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");

        FirmEntity newFirm = firmService.createFirm(firmRegisterDto);

        int newFirmsNum = firmRepository.findAll().size();
        UserEntity adminFromRepo = userRepository.findByEmail(firmRegisterDto.getAdminEmail()).orElse(null);
        UserEntity admin = newFirm.getUsers().get(0);

        Assertions.assertEquals(firmsNum + 1, newFirmsNum);
        Assertions.assertEquals(firmRegisterDto.getFirmName(), newFirm.getFirmName());
        Assertions.assertNotNull(adminFromRepo);
        Assertions.assertEquals(admin, adminFromRepo);
    }

    @Test
    void shouldNotCreateNewFirm1() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm From Test");
        firmRegisterDto.setAdminName("Admin From Test");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");

        firmService.createFirm(firmRegisterDto);

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmRegisterDto));
    }

    @Test
    void shouldNotCreateNewFirm2() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm From Test");
        firmRegisterDto.setAdminName("Admin From Test");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");

        firmService.createFirm(firmRegisterDto);

        firmRegisterDto.setFirmName("Firm From Test");
        firmRegisterDto.setAdminName("Diffrent Admin From Test");
        firmRegisterDto.setAdminEmail("admin2@firmtest.com");
        firmRegisterDto.setPassword("password");

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmRegisterDto));
    }

    @Test
    void shouldNotCreateNewFirm3() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm From Test");
        firmRegisterDto.setAdminName("Admin From Test");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");
        firmService.createFirm(firmRegisterDto);

        firmRegisterDto.setFirmName("Firm From Test 2");
        firmRegisterDto.setAdminName("Admin From Test");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");

        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.createFirm(firmRegisterDto));
    }

    @Test
    void shouldNotCreateNewFirm4() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.createFirm(firmRegisterDto));
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

//    TODO: NEW TESTS WITH AUTHENTICATION
//    @Test
//    void shouldAddDeviceToFirm() {
//        FirmEntity firm = this.createFirm();
//
//        DeviceDto deviceDto = new DeviceDto();
//        deviceDto.setName("Forklift");
//
//        DeviceEntity forklift =  firmService.addDeviceToFirm(firm, deviceDto);
//
//        Assertions.assertNotNull(forklift);
//        Assertions.assertEquals("Forklift", forklift.getName());
//        Assertions.assertEquals(firm, forklift.getFirm());
//    }
//
//    @Test
//    void shouldNotAddDeviceToFirm() {
//        FirmEntity firm = this.createFirm();
//        DeviceDto deviceDto = new DeviceDto();
//        deviceDto.setName("Forklift");
//        firmService.addDeviceToFirm(firm, deviceDto);
//
//        Assertions.assertThrows(ResourceAlreadyExistsException.class, () -> firmService.addDeviceToFirm(firm, deviceDto));
//    }
//
//    @Test
//    void shouldNotAddDeviceToFirm1() {
//        FirmEntity firm = this.createFirm();
//        DeviceDto deviceDto = new DeviceDto();
//        Assertions.assertThrows(IllegalArgumentException.class, () -> firmService.addDeviceToFirm(firm, deviceDto));
//    }

    private FirmEntity createFirm() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm");
        firmRegisterDto.setAdminName("Admin");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");
        return firmService.createFirm(firmRegisterDto);
    }
}
