package kcz.rfid.backend;

import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.repository.*;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.LocationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class DeviceServiceTests {

    @Autowired
    private FirmRepository firmRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private LocationHistoryRepository locationHistoryRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private FirmService firmService;

    private FirmEntity firm;
    private DeviceEntity forklift;
    private List<LocationEntity> locations;
    @Autowired
    private LocationService locationService;

    @BeforeEach
    public void setUp() {
        locationHistoryRepository.deleteAll();
        deviceRepository.deleteAll();
        locationRepository.deleteAll();
        firmRepository.deleteAll();

        this.firm  = createFirm();
        this.forklift = createDevice();
        addLocationsToFirm();
    }

    private FirmEntity createFirm() {
        FirmRegisterDto firmRegisterDto = new FirmRegisterDto();
        firmRegisterDto.setFirmName("Firm");
        firmRegisterDto.setAdminName("Admin");
        firmRegisterDto.setAdminEmail("admin@firmtest.com");
        firmRegisterDto.setPassword("password");
        return firmService.createFirm(firmRegisterDto);
    }

    private DeviceEntity createDevice() {
        DeviceRegisterDto deviceDto = new DeviceRegisterDto();
        deviceDto.setDeviceName("Forklift");
        deviceDto.setPublicKey("publicKey");

        return deviceService.createDevice(deviceDto, firm);
    }

    private void addLocationsToFirm() {
        LocationDto locationDto = new LocationDto();
        locationDto.setName("Location1");
        locationDto.setZoneId(1);
        locationDto.setX(0);
        locationDto.setY(0);
        firmService.addLocationToFirm(firm, locationDto);

        locationDto.setName("Location2");
        locationDto.setZoneId(2);
        locationDto.setX(10);
        locationDto.setY(10);
        firmService.addLocationToFirm(firm, locationDto);

        locationDto.setName("Location3");
        locationDto.setZoneId(3);
        locationDto.setX(20);
        locationDto.setY(20);
        firmService.addLocationToFirm(firm, locationDto);

        locations = locationRepository.findAllByFirmId(firm.getId());
    }

    @Test
    public void locationShouldBeNull() {
        LocationEntity location1 = forklift.getLocation();
        Assertions.assertNull(location1);
    }

    @Test
    public void shouldUpdateLocation() {
        LocationEntity location1 = locations.get(0);
        deviceService.updateLocation(forklift, location1);

        DeviceEntity fFromDb = deviceRepository.findById(forklift.getId()).orElse(null);
        Assertions.assertNotNull(fFromDb);
        LocationEntity l1 = forklift.getLocation();

        Assertions.assertNotNull(l1);
        Assertions.assertEquals(location1, l1);
        Assertions.assertEquals(location1.getZoneId(), l1.getZoneId());

        List<LocationHistoryEntity> locationHistory = locationService.getLocationHistoryForFirm(firm);
        Assertions.assertEquals(1, locationHistory.size());
        Assertions.assertEquals(location1, locationHistory.get(0).getLocation());
        Assertions.assertEquals(forklift, locationHistory.get(0).getDevice());
    }

    @Test
    public void shouldWriteAllLocationHistory() {
        for (var location : locations) {
            deviceService.updateLocation(forklift, location);
        }

        List<LocationHistoryEntity> locationHistory = locationService.getLocationHistoryForForklift(forklift);
        List<LocationHistoryEntity> locationHistory2 = locationService.getLocationHistoryForFirm(firm);
        DeviceEntity forkliftDb = deviceRepository.findById(forklift.getId()).orElse(null);
        Assertions.assertNotNull(forkliftDb);

        Assertions.assertEquals(locations.size(), locationHistory.size());
        Assertions.assertEquals(locations.size(), locationHistory2.size());
        Assertions.assertEquals(forkliftDb.getLocation(), locations.get(locations.size() - 1));
    }
}
