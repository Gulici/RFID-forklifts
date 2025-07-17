package kcz.rfid.backend.controller;

import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.FirmMapper;
import kcz.rfid.backend.model.mapper.UserMapper;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/firms")
@RequiredArgsConstructor
public class FirmController {
    private final FirmService firmService;
    private final FirmMapper firmMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<FirmEntity> createFirm(@RequestBody FirmRegisterDto dto) {
        FirmEntity firm = firmService.createFirm(dto);
        return new ResponseEntity<>(firm, HttpStatus.CREATED);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<FirmDto>> getAllFirms() {
        Collection<FirmEntity> firms = firmService.getAll();
        return new ResponseEntity<>(firms.stream().map(firmMapper::mapToDto).toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROOT') or @securityService.isUserOfFirm(authentication.principal, #id)")
    public ResponseEntity<FirmDto> getFirmById(@PathVariable UUID id) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        return ResponseEntity.ok(firmMapper.mapToDto(firm));
    }

    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ROOT') or @securityService.isAdminOfFirm(authentication.principal, #id)")
    public ResponseEntity<List<UserDto>> geFirmUsers(@PathVariable UUID id) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        List<UserEntity> users = userService.findUsersByFirm(firm);
        return ResponseEntity.ok(users.stream().map(userMapper::mapToDto).toList());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROOT') or @securityService.isAdminOfFirm(authentication.principal, #id)")
    public ResponseEntity<FirmDto> updateFirm(@PathVariable UUID id, @RequestBody FirmDto dto) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        firm = firmService.updateFirm(firm, dto);
        return ResponseEntity.ok(firmMapper.mapToDto(firm));
    }
}
