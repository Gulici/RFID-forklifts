package kcz.rfid.backend;

import kcz.rfid.backend.model.entity.RoleEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.RoleRepository;
import kcz.rfid.backend.model.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public BackendApplication(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        createTestData();
    }

    private void createTestData() {
        createRoles();
        createUsers();
    }

    private void createRoles() {
        if (roleRepository.findAll().isEmpty()) {
            RoleEntity admin = new RoleEntity();
            admin.setName(RoleEnum.ROLE_ADMIN);
            roleRepository.save(admin);

            RoleEntity user = new RoleEntity();
            user.setName(RoleEnum.ROLE_USER);
            roleRepository.save(user);
        }
    }

    private void createUsers() {
        if (userRepository.findAll().isEmpty()) {
            RoleEntity adminRole = roleRepository.findRoleEntityByName(RoleEnum.ROLE_ADMIN).get();
            RoleEntity userRole = roleRepository.findRoleEntityByName(RoleEnum.ROLE_USER).get();

            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPassword("admin");
            admin.setEmail("admin@mail.com");
            HashSet<RoleEntity> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(userRole);
            admin.setRoles(roles);
            userRepository.save(admin);

            UserEntity user = new UserEntity();
            user.setUsername("user");
            user.setPassword("password");
            user.setEmail("user@mail.com");
            HashSet<RoleEntity> userRoles = new HashSet<>();
            userRoles.add(userRole);
            user.setRoles(userRoles);
            userRepository.save(user);
        }
    }
}
