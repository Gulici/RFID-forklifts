package kcz.rfid.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kcz.rfid.backend.config.security.SecurityService;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.dto.LocationHistoryDto;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.LocationHistoryEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.LocationHistoryMapper;
import kcz.rfid.backend.model.mapper.LocationMapper;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@Tag(name = "Location", description = "Operations related to location management")
public class LocationController {
    private final UserService userService;
    private final FirmService firmService;
    private final LocationMapper locationMapper;
    private final LocationService locationService;
    private final SecurityService securityService;
    private final LocationHistoryMapper locationHistoryMapper;

    @Operation(summary = "Create a new location", description = "Creates a new location associated with the admin's firm. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Location created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ADMIN role)",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        LocationEntity location = firmService.addLocationToFirm(admin.getFirm(), dto);

        return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all locations", description = "Returns all locations for the admin's firm. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of locations",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ADMIN role)",
                    content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationDto>> getAllLocations(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        List<LocationEntity> locations = locationService.getLocationsByFirm(admin.getFirm());

        return ResponseEntity.ok(locations.stream().map(locationMapper::mapToDto).toList());
    }

    @Operation(summary = "Get location by ID", description = "Returns location details by its ID. Accessible to ROOT users or users of the location's firm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT or not user of firm)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (securityService.isRoot(userDetails) || securityService.isUserOfFirm(userDetails, location.getFirm())) {
            return ResponseEntity.ok(locationMapper.mapToDto(location));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @Operation(summary = "Update location by ID", description = "Updates location details. Requires ADMIN role and user must be admin of the location's firm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Location updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LocationDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ADMIN or not admin of firm)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDto> updateLocationById(@PathVariable UUID id, @RequestBody LocationDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (securityService.isAdminOfFirm(userDetails, location.getFirm())) {
            location = locationService.updateLocation(dto, id);
            return ResponseEntity.ok(locationMapper.mapToDto(location));
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @Operation(summary = "Delete location by ID", description = "Deletes the location. Requires ADMIN role and user must be admin of the location's firm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied (not ADMIN or not admin of firm)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Location not found",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocationById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (securityService.isAdminOfFirm(userDetails, location.getFirm())) {
            locationService.delete(location);
            return ResponseEntity.noContent().build();
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/history")
    public ResponseEntity<List<LocationHistoryDto>> getAllLocationHistory(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.findUserByUsername(userDetails.getUsername());

        List<LocationHistoryEntity> locations = locationService.getLocationHistoryForFirm(user.getFirm());

        return ResponseEntity.ok(locations.stream().map(locationHistoryMapper::mapToDto).toList());
    }
}
