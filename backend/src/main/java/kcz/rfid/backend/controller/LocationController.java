package kcz.rfid.backend.controller;

import kcz.rfid.backend.config.security.SecurityService;
import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;
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
public class LocationController {
    private final UserService userService;
    private final FirmService firmService;
    private final LocationMapper locationMapper;
    private final LocationService locationService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        LocationEntity location = firmService.addLocationToFirm(admin.getFirm(), dto);

        return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LocationDto>> getAllLocations(@AuthenticationPrincipal UserDetails userDetails) {
        UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
        List<LocationEntity> locations = locationService.getLocationsByFirm(admin.getFirm());

        return ResponseEntity.ok(locations.stream().map(locationMapper::mapToDto).toList());
    }


    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location not found"));

        if (securityService.isRoot(userDetails) || securityService.isUserOfFirm(userDetails, location.getFirm())) {
            return ResponseEntity.ok(locationMapper.mapToDto(location));
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

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
}
