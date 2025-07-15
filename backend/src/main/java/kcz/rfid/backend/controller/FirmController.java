package kcz.rfid.backend.controller;

import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.FirmDto;
import kcz.rfid.backend.model.dto.FirmRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.FirmMapper;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/firms")
public class FirmController {
    private final FirmService firmService;
    private final FirmMapper firmMapper;
    private final UserService userService;

    public FirmController(FirmService firmService, FirmMapper firmMapper, UserService userService) {
        this.firmService = firmService;
        this.firmMapper = firmMapper;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<FirmEntity> createFirm(@RequestBody FirmRegisterDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        FirmEntity firm = firmService.createFirm(dto);
        return new ResponseEntity<>(firm, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<FirmDto>> getAllFirms(@AuthenticationPrincipal UserDetails userDetails) {
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Collection<FirmEntity> firms = firmService.getAll();
        return new ResponseEntity<>(firms.stream().map(firmMapper::mapToDto).toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FirmDto> getFirmById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        FirmEntity firm = firmService.findById(id);
        if (firm == null) {
            throw new ResourceNotFoundException("Firm with id " + id + " not found");
        }
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            UserEntity user = userService.findUserByUsername(userDetails.getUsername());
            if (user.getFirm().equals(firm)) {
                return new ResponseEntity<>(firmMapper.mapToDto(firm), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        return new ResponseEntity<>(firmMapper.mapToDto(firm), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FirmDto> updateFirm(@PathVariable UUID id, @RequestBody FirmDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        FirmEntity firm = firmService.findById(id);
        if (firm == null) {
            throw new ResourceNotFoundException("Firm with id " + id + " not found");
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (admin.getFirm().equals(firm)) {
                firm = firmService.updateFirm(firm, dto);
                return new ResponseEntity<>(firmMapper.mapToDto(firm), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        firm = firmService.updateFirm(firm, dto);
        return new ResponseEntity<>(firmMapper.mapToDto(firm), HttpStatus.OK);
    }
}
