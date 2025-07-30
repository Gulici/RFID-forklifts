package kcz.rfid.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.Enabled;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "Operations related to user management")
public class UserController {

    private final UserService userService;
    private final FirmService firmService;
    private final UserMapper userMapper;
    private final SecurityService securityService;

    @Operation(summary = "Register user to firm", description = "Allows ADMIN to register a new user under their firm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ADMIN)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> registerUserToFirm(@RequestBody @Valid UserRegisterDto dto, @AuthenticationPrincipal UserDetails userDetails) {

        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        FirmEntity firmEntity = admin.getFirm();
        UserEntity user = firmService.addUserToFirm(firmEntity,dto);

        return new ResponseEntity<>(userMapper.mapToDto(user), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all users (ROOT only)", description = "Returns list of all users, requires ROOT role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping()
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        Collection<UserEntity> users = userService.getAll();
        return new ResponseEntity<>(users.stream().map(userMapper::mapToDto).toList(), HttpStatus.OK);
    }

    @Operation(summary = "Get user details by ID", description = "Returns user details if requester is the user, ROOT, or admin of user's firm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
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

    @Operation(summary = "Delete user by ID", description = "Deletes user if requester has ADMIN or ROOT role and access rights")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
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
