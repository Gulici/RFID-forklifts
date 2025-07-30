package kcz.rfid.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kcz.rfid.backend.config.security.DeviceAuthenticationToken;
import kcz.rfid.backend.config.security.SecurityService;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.DeviceDto;
import kcz.rfid.backend.model.dto.DeviceLocationDto;
import kcz.rfid.backend.model.dto.DeviceRegisterDto;
import kcz.rfid.backend.model.dto.DeviceUpdateDto;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.DeviceMapper;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Device", description = "Operations related to device management")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceMapper deviceMapper;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final FirmService firmService;
    private final SecurityService securityService;

    @Operation(summary = "Register a new device", description = "Allows admin to register a new device linked to their firm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not admin)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<DeviceDto> registerDevice(@RequestBody DeviceRegisterDto registerDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(registerDto.getUsername(), registerDto.getPassword())
        );

        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (isAdmin) {
            UserEntity admin = userService.findUserByUsername(registerDto.getUsername());
            DeviceEntity device = firmService.addDeviceToFirm(admin.getFirm(), registerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(deviceMapper.mapToDto(device));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Get all devices (ROOT only)", description = "Returns a list of all devices in the system, requires ROOT role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of devices",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<DeviceDto>> getAllDevicesRoot(){
        return ResponseEntity.ok(deviceService.getAll().stream().map(deviceMapper::mapToDto).toList());
    }

    @Operation(summary = "Get devices for admins firm", description = "Returns devices belonging to the admins firm, requires ADMIN role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of devices",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DeviceDto>> getAllDevicesAdmin(@AuthenticationPrincipal UserDetails userDetails){
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(deviceService.findDevicesByFirm(admin.getFirm()).stream().map(deviceMapper::mapToDto).toList());
    }

    @Operation(summary = "Get device by ID", description = "Returns device details by device ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDto> getDeviceById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        DeviceEntity device = deviceService.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Device with id " + id + " not found")
        );

        if (securityService.isRoot(userDetails) || securityService.isUserOfFirm(userDetails, device.getFirm())) {
            return ResponseEntity.ok(deviceMapper.mapToDto(device));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Update device", description = "Updates device data, requires ADMIN or ROOT roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeviceDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<DeviceDto> updateDevice(@RequestBody DeviceUpdateDto dto, @AuthenticationPrincipal UserDetails userDetails, @PathVariable UUID id) {
        DeviceEntity device = deviceService.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Device with id " + id + " not found")
        );

        if (securityService.isAdminOfFirm(userDetails, device.getFirm()) || securityService.isRoot(userDetails)) {
            device.setName(dto.getName());
            deviceService.save(device);
            return ResponseEntity.ok(deviceMapper.mapToDto(device));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Delete device", description = "Deletes device by ID, requires ADMIN or ROOT roles")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        DeviceEntity device = deviceService.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Device with id " + id + " not found")
        );

        if (securityService.isAdminOfFirm(userDetails, device.getFirm()) || securityService.isRoot(userDetails)) {
            deviceService.delete(device);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Update device location", description = "Device updates its own location, requires DEVICE role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request (device ID or firm ID mismatch)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not authenticated as device)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/updateLocation")
    @PreAuthorize("hasRole('DEVICE')")
    public ResponseEntity<Void> updateDeviceLocation(@RequestBody DeviceLocationDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (!(auth instanceof DeviceAuthenticationToken deviceAuth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UUID deviceId = deviceAuth.getDeviceId();
        UUID firmId = deviceAuth.getFirmId();

        boolean valid = Objects.equals(dto.getId(), deviceId) && Objects.equals(dto.getFirmId(), firmId);
        if (!valid) {
            return ResponseEntity.badRequest().build();
        }

        firmService.updateDeviceLocation(dto);
        return ResponseEntity.ok().build();
    }
}
