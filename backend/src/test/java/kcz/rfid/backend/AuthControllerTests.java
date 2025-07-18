package kcz.rfid.backend;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import kcz.rfid.backend.config.security.JwtService;
import kcz.rfid.backend.model.dto.*;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.repository.UserRepository;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.utils.PemUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.security.KeyPair;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static kcz.rfid.backend.service.utils.PemUtils.computeFingerprint;

@AutoConfigureMockMvc
@SpringBootTest
@Testcontainers
public class AuthControllerTests {

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
    private DeviceService deviceService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    @Test
    void shouldAuthenticateUserAndReturnJwt() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin1");
        loginRequest.setPassword("password");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        String token = JsonPath.read(jsonResponse, "$.jwt");

        Assertions.assertEquals("USER", jwtService.extractTokenType(token));
        Assertions.assertEquals("admin1", jwtService.extractUsername(token));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin1");
        loginRequest.setPassword("wrong-password");

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isUnauthorized());
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
}
