package kcz.rfid.backend.controller;

import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.DeviceMapper;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/forklifts")
public class ForkliftController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;
    private final UserService userService;

    public ForkliftController(DeviceService deviceService, DeviceMapper deviceMapper, UserService userService) {
        this.deviceService = deviceService;
        this.deviceMapper = deviceMapper;
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<List<DeviceDto>> getAllForklifts(@AuthenticationPrincipal UserDetails userDetails) {
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
    public ResponseEntity<DeviceDto> getForkliftById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        DeviceEntity forklift = deviceService.findById(id);
        if (forklift == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(deviceMapper.mapToDto(forklift), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (admin.getFirm().equals(forklift.getFirm())) {
                return new ResponseEntity<>(deviceMapper.mapToDto(forklift), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

}
