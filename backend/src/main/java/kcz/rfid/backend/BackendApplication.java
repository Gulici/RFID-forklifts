package kcz.rfid.backend;

import kcz.rfid.backend.model.entity.*;
import kcz.rfid.backend.model.entity.util.RoleEnum;
import kcz.rfid.backend.model.repository.FirmRepository;
import kcz.rfid.backend.model.repository.ForkliftRepository;
import kcz.rfid.backend.model.repository.RoleRepository;
import kcz.rfid.backend.model.repository.UserRepository;
import kcz.rfid.backend.service.InitService;
import org.hibernate.Hibernate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

    private final InitService initService;

    public BackendApplication(InitService initService) {
        this.initService = initService;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        createTestData();
    }

    private void createTestData() {
        initService.createFirmAndForklifts();
        initService.createRoles();
        initService.createUsers();
    }
}
