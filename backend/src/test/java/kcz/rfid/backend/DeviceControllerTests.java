package kcz.rfid.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import kcz.rfid.backend.config.security.JwtService;
import kcz.rfid.backend.model.dto.DeviceLocationDto;
import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.utils.PemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.KeyPair;
import java.util.UUID;

import static kcz.rfid.backend.service.utils.PemUtils.computeFingerprint;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DeviceControllerTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("rfid_db_test")
            .withUsername("admin")
            .withPassword("admin");

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private JwtService jwtService;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void shouldAuthenticateAdminAndRegisterDevice() throws Exception {
        KeyPair keyPair = TestUtils.generateKeyPair();
        String publicKeyPem = PemUtils.getPemPublicKey(keyPair.getPublic());

        DeviceRegisterDto registerDto = new DeviceRegisterDto();
        registerDto.setUsername("admin1");
        registerDto.setPassword("password");
        registerDto.setDeviceName("dev1");
        registerDto.setPublicKey(publicKeyPem);

        TestUtils.registerDevice(mockMvc, objectMapper, registerDto);
        DeviceEntity device = deviceService.findDeviceByFingerprint(computeFingerprint(publicKeyPem));

        String nonce = TestUtils.requestNonce(mockMvc, objectMapper, publicKeyPem);
        String signatureBase64 = TestUtils.signNonce(nonce, keyPair.getPrivate());
        String token = TestUtils.verifySignatureAndGetToken(mockMvc, objectMapper, publicKeyPem, signatureBase64);

        Assertions.assertEquals("DEVICE", jwtService.extractTokenType(token));
        Assertions.assertEquals(device.getId(), jwtService.extractDeviceId(token));
        Assertions.assertEquals(device.getFirm().getId(), jwtService.extractCompanyId(token));
        Assertions.assertTrue(jwtService.validateDeviceToken(token, device.getId()));
    }

    @Test
    void shouldAuthenticateAdminAndRegisterDeviceUtilMethod() throws Exception {
        KeyPair keyPair = TestUtils.generateKeyPair();

        DeviceRegisterDto registerDto = new DeviceRegisterDto();
        registerDto.setUsername("admin1");
        registerDto.setPassword("password");
        registerDto.setDeviceName("dev2");
        registerDto.setPublicKey(PemUtils.getPemPublicKey(keyPair.getPublic()));

        String token = TestUtils.registerAndAuthorizeNewDevice(mockMvc, objectMapper, registerDto, keyPair);
        DeviceEntity device = deviceService.findDeviceByFingerprint(computeFingerprint(registerDto.getPublicKey()));
        Assertions.assertTrue(jwtService.validateDeviceToken(token, device.getId()));
    }

    @Test
    void shouldUpdateDeviceLocation() throws Exception {
        KeyPair keyPair = TestUtils.generateKeyPair();

        DeviceRegisterDto registerDto = new DeviceRegisterDto();
        registerDto.setUsername("admin1");
        registerDto.setPassword("password");
        registerDto.setDeviceName("dev3");
        registerDto.setPublicKey(PemUtils.getPemPublicKey(keyPair.getPublic()));

        String token = TestUtils.registerAndAuthorizeNewDevice(mockMvc, objectMapper, registerDto, keyPair);
        UUID deviceId = jwtService.extractDeviceId(token);
        UUID firmId = jwtService.extractCompanyId(token);

        DeviceLocationDto dto = new DeviceLocationDto();
        dto.setId(deviceId);
        dto.setFirmId(firmId);
        dto.setEpcCode("ffff01ff");

        mockMvc.perform(put("/devices/updateLocation")
                        .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        DeviceEntity device = deviceService.findDeviceByFingerprint(computeFingerprint(registerDto.getPublicKey()));
        Assertions.assertEquals(1, device.getLocation().getZoneId());

        dto.setEpcCode("ffff02ff");

        mockMvc.perform(put("/devices/updateLocation")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        device = deviceService.findDeviceByFingerprint(computeFingerprint(registerDto.getPublicKey()));
        Assertions.assertEquals(2, device.getLocation().getZoneId());
    }
}
