package kcz.rfid.backend;

import kcz.rfid.backend.service.InitService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
        initService.createRoles();
        initService.testFirmCreation();
    }
}
