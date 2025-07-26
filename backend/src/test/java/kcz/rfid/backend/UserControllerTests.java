package kcz.rfid.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import kcz.rfid.backend.model.dto.LoginRequest;
import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerTests {

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
    private UserService userService;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    public void shouldAddUserToFirm() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin1");
        request.setPassword("password");

        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("user1");
        dto.setPassword("password");
        dto.setEmail("user1@firm1.com");
        dto.setFirmName("test-firm1");

        MvcResult result = mockMvc.perform(post("/users/register")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        UserDto userDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

        Assertions.assertEquals(dto.getUsername(), userDto.getUsername());
        Assertions.assertEquals(dto.getEmail(), userDto.getEmail());
        Assertions.assertEquals(dto.getFirmName(), userDto.getFirmName());

        UserEntity user = userService.findUserByUsername(userDto.getUsername());
        Assertions.assertNotNull(user);
        Assertions.assertEquals(dto.getEmail(), user.getEmail());
        Assertions.assertEquals(dto.getUsername(), user.getUsername());
        Assertions.assertEquals(user.getFirm().getFirmName(), dto.getFirmName());

        request = new LoginRequest();
        request.setUsername("user1");
        request.setPassword("password");
        String token2 = TestUtils.loginUser(mockMvc, objectMapper, request);
    }

    @Test
    public void shouldNotAddUserToFirm() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin1");
        request.setPassword("password");
        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("admin1");
        dto.setPassword("password");
        dto.setEmail("user2@firm1.com");
        dto.setFirmName("test-firm1");

        mockMvc.perform(post("/users/register")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotAddUserToFirmWithInvalidToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("root");
        request.setPassword("password");
        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("username");
        dto.setPassword("password");
        dto.setEmail("user3@firm1.com");
        dto.setFirmName("test-firm1");

        mockMvc.perform(post("/users/register")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldNotAddUserToFirmUnauthorized() throws Exception {
        UserRegisterDto dto = new UserRegisterDto();
        dto.setUsername("username");
        dto.setPassword("password");
        dto.setEmail("user4@firm1.com");
        dto.setFirmName("test-firm1");

        mockMvc.perform(post("/users/register")
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void shouldNotAddUserToFirmBadRequest() throws Exception {

        LoginRequest request = new LoginRequest();
        request.setUsername("admin1");
        request.setPassword("password");
        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        UserRegisterDto dto = new UserRegisterDto();

        mockMvc.perform(post("/users/register")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetUserDetails() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin1");
        request.setPassword("password");
        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000007");

        MvcResult result = mockMvc.perform(get("/users/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").exists())
                .andReturn();

        UserDto userDto = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

        Assertions.assertEquals(id, userDto.getId());
        Assertions.assertEquals(request.getUsername(), userDto.getUsername());
        Assertions.assertNotNull(userDto.getEmail());
        Assertions.assertNotNull(userDto.getFirmName());
    }

    @Test
    public void shouldGetAllUsers() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("root");
        request.setPassword("password");
        String token = TestUtils.loginUser(mockMvc, objectMapper, request);

        MvcResult result = mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }
}
