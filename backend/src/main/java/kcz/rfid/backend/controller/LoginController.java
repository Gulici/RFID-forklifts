package kcz.rfid.backend.controller;

import kcz.rfid.backend.config.security.JwtService;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LoginDeviceRequest;
import kcz.rfid.backend.model.dto.LoginRequest;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.service.DeviceService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DeviceService deviceService;

    public LoginController(AuthenticationManager authenticationManager, JwtService jwtService, DeviceService deviceService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.deviceService = deviceService;
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        if (auth.isAuthenticated()) {
            return jwtService.generateToken(dto.getUsername());
        } else {
            throw new ResourceNotFoundException("Invalid username or password");
        }
    }

    //TODO: Development implementation only, for future changes!! Replace with full authorization based on nonce
    @PostMapping("/device_login")
    public String loginDevice(LoginDeviceRequest dto) {
        DeviceEntity device = deviceService.findDeviceByPublicKey(dto.getPublicKey());
        return jwtService.generateDeviceToken(device.getId(), device.getLocation().getId());
    }
}
