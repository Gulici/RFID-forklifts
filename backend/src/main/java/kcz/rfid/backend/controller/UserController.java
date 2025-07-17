package kcz.rfid.backend.controller;

import jakarta.validation.Valid;
import kcz.rfid.backend.config.security.SecurityService;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.UserMapper;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FirmService firmService;
    private final UserMapper userMapper;
    private final SecurityService securityService;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> registerUserToFirm(@RequestBody @Valid UserRegisterDto dto, @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        FirmEntity firmEntity = admin.getFirm();
        UserEntity user = firmService.addUserToFirm(firmEntity,dto);

        return new ResponseEntity<>(userMapper.mapToDto(user), HttpStatus.CREATED);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        Collection<UserEntity> users = userService.getAll();
        return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserDetailById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity user = userService.findUserByUsername(userDetails.getUsername());
        UserEntity requestedUser= userService.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));

        if (user.getId().equals(requestedUser.getId()) || securityService.isRoot(userDetails)
                || securityService.isAdminOfFirm(userDetails, requestedUser.getFirm())) {
            return ResponseEntity.ok(userMapper.mapToDto(requestedUser));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found"));

        if (securityService.isRoot(userDetails) || securityService.isAdminOfFirm(userDetails, user.getFirm())) {
            userService.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}
