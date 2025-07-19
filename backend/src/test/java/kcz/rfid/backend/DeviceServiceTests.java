package kcz.rfid.backend;

import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.repository.*;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.utils.PemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.List;

@SpringBootTest
@Testcontainers
public class DeviceServiceTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("rfid_db_test")
            .withUsername("admin")
            .withPassword("admin");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private FirmRepository firmRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private DeviceService deviceService;

    private FirmEntity firm;
    private DeviceEntity device;
    private List<LocationEntity> locations;
    @Autowired
    private LocationService locationService;


    @BeforeEach
    public void setUp() throws NoSuchAlgorithmException {
        deviceRepository.deleteAll();

        firm = firmRepository.findByFirmName("test-firm1").orElseThrow();
        locations = locationService.getLocationsByFirm(firm);

        device = createDevice();
    }

    private DeviceEntity createDevice() throws NoSuchAlgorithmException {

        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        String publicKeyPem = PemUtils.getPemPublicKey(publicKey);

        DeviceRegisterDto deviceDto = new DeviceRegisterDto();
        deviceDto.setDeviceName("test-device");
        deviceDto.setPublicKey(publicKeyPem);

        return deviceService.createDevice(deviceDto, firm);
    }

    @Test
    public void locationShouldBeNull() {
        LocationEntity location1 = device.getLocation();
        Assertions.assertNull(location1);
    }

    @Test
    public void shouldUpdateLocation() {
        LocationEntity location1 = locations.get(0);
        deviceService.updateLocation(device, location1);

        DeviceEntity fFromDb = deviceRepository.findById(device.getId()).orElse(null);
        Assertions.assertNotNull(fFromDb);
        LocationEntity l1 = device.getLocation();

        Assertions.assertNotNull(l1);
        Assertions.assertEquals(location1, l1);
        Assertions.assertEquals(location1.getZoneId(), l1.getZoneId());

        List<LocationHistoryEntity> locationHistory = locationService.getLocationHistoryForFirm(firm);
        Assertions.assertEquals(1, locationHistory.size());
        Assertions.assertEquals(location1, locationHistory.get(0).getLocation());
        Assertions.assertEquals(device, locationHistory.get(0).getDevice());
    }

    @Test
    public void shouldWriteAllLocationHistory() {
        for (var location : locations) {
            deviceService.updateLocation(device, location);
        }

        List<LocationHistoryEntity> locationHistory = locationService.getLocationHistoryForForklift(device);
        List<LocationHistoryEntity> locationHistory2 = locationService.getLocationHistoryForFirm(firm);
        DeviceEntity forkliftDb = deviceRepository.findById(device.getId()).orElse(null);
        Assertions.assertNotNull(forkliftDb);

        Assertions.assertEquals(locations.size(), locationHistory.size());
        Assertions.assertEquals(locations.size(), locationHistory2.size());
        Assertions.assertEquals(forkliftDb.getLocation(), locations.get(locations.size() - 1));
    }
}
