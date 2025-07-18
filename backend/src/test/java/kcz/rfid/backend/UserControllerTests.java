package kcz.rfid.backend;

import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;



@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTests {

//    @Autowired
//    private MockMvc mockMvc;
//
//    private final UserRepository userRepository;
//
//    private UserEntity root;
//    private UserEntity admin1;
//    private UserEntity admin2;
//
//    public UserControllerTests(UserRepository userRepository) {
//        this.userRepository = userRepository;
//        root = userRepository.findByUsername("root").orElseThrow(() -> new RuntimeException("Root user not found"));
//        admin1 = userRepository.findByUsername("admin1").orElseThrow(() -> new RuntimeException("Admin1 user not found"));
//        admin2 = userRepository.findByUsername("admin2").orElseThrow(() -> new RuntimeException("Admin2 user not found"));
//    }
}
