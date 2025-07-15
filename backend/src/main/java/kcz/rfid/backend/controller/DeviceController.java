package kcz.rfid.backend.controller;

import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.RegisterDeviceDto;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.DeviceMapper;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forklifts")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;
    private final UserService userService;

    private final AuthenticationManager authenticationManager;
    private final FirmService firmService;

    public DeviceController(DeviceService deviceService, DeviceMapper deviceMapper, UserService userService, AuthenticationManager authenticationManager, FirmService firmService) {
        this.deviceService = deviceService;
        this.deviceMapper = deviceMapper;
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.firmService = firmService;
    }

    @PostMapping
    public ResponseEntity<DeviceDto> createDevice(@RequestBody RegisterDeviceDto registerDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerDto.getUsername(), registerDto.getPassword())
        );
        if (auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(registerDto.getUsername());
            DeviceEntity device = firmService.addDeviceToFirm(admin.getFirm(), registerDto);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping()
    public ResponseEntity<List<DeviceDto>> getAllDevices(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(deviceService.getAll().stream().map(deviceMapper::mapToDto).toList(), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            return new ResponseEntity<>(deviceService.findDevicesByFirm(admin.getFirm()).stream().map(deviceMapper::mapToDto).toList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        DeviceEntity device = deviceService.findById(id);
        if (device == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(deviceMapper.mapToDto(device), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (admin.getFirm().equals(device.getFirm())) {
                return new ResponseEntity<>(deviceMapper.mapToDto(device), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
