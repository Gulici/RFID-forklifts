package kcz.rfid.backend.controller;

import jakarta.validation.Valid;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.UserDto;
import kcz.rfid.backend.model.dto.UserRegisterDto;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.UserMapper;
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
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final FirmService firmService;
    private final UserMapper userMapper;

    public UserController(UserService userService, FirmService firmService, UserMapper userMapper) {
        this.userService = userService;
        this.firmService = firmService;
        this.userMapper = userMapper;
    }

    @PostMapping("/admin/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid UserRegisterDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        if (admin == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        FirmEntity firmEntity = admin.getFirm();

        UserEntity user = firmService.addUserToFirm(firmEntity,dto);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(userMapper.mapToDto(user), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserDetailById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        UserEntity user = userService.findUserByUsername(userDetails.getUsername());
        UserEntity requestedUser= userService.findById(id);
        if (requestedUser == null) {
            throw new ResourceNotFoundException("User with id " + id + " not found");
        }

        if (user.getId().equals(requestedUser.getId()) ||
                userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(userMapper.mapToDto(requestedUser), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")) &&
            user.getFirm().equals(requestedUser.getFirm())) {
            return new ResponseEntity<>(userMapper.mapToDto(requestedUser), HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/firm/{id}")
    public ResponseEntity<List<UserDto>> getAllUsersByFirmId(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            FirmEntity firmEntity = firmService.findById(id);
            if (firmEntity == null) {
                throw new ResourceNotFoundException("Firm with id " + id + " not found");
            }
            List<UserEntity> users = userService.findUsersByFirm(firmEntity);
            return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (admin.getFirm().getId().equals(id)) {
                FirmEntity firmEntity = admin.getFirm();
                List<UserEntity> users = userService.findUsersByFirm(firmEntity);
                return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping()
    public ResponseEntity<List<UserDto>> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            Collection<UserEntity> users = userService.getAll();
            return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            List<UserEntity> users = userService.findUsersByFirm(admin.getFirm());
            return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            UserEntity rootUser = userService.findUserByUsername(userDetails.getUsername());
            if (rootUser.getId().equals(id)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            userService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity user = userService.findById(id);
            if (user == null) {
                throw new ResourceNotFoundException("User with id " + id + " not found");
            }
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (admin.getFirm().equals(user.getFirm()) && !admin.equals(user)) {
                userService.delete(user);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
