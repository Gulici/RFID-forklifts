package kcz.rfid.backend.controller;

import kcz.rfid.backend.exception.ResourceNotFoundException;
import kcz.rfid.backend.model.dto.LocationDto;
import kcz.rfid.backend.model.entity.LocationEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.model.mapper.LocationMapper;
import kcz.rfid.backend.service.FirmService;
import kcz.rfid.backend.service.LocationService;
import kcz.rfid.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final UserService userService;
    private final FirmService firmService;
    private final LocationMapper locationMapper;
    private final LocationService locationService;

    public LocationController(UserService userService, FirmService firmService, LocationMapper locationMapper, LocationService locationService) {
        this.userService = userService;
        this.firmService = firmService;
        this.locationMapper = locationMapper;
        this.locationService = locationService;
    }

    @PostMapping
    public ResponseEntity<LocationDto> createLocation(@RequestBody LocationDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            LocationEntity location = firmService.addLocationToFirm(admin.getFirm(), dto);
            return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.CREATED);
        }
        else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping
    public ResponseEntity<List<LocationDto>> getAllLocations(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            List<LocationEntity> locations = locationService.getLocationsByFirm(admin.getFirm());
            return new ResponseEntity<>(locations.stream().map(locationMapper::mapToDto).toList(), HttpStatus.OK);
        }
        else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDto> getLocationById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException("Location with id " + id + " not found");
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (location.getFirm().equals(admin.getFirm())) {
                return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        } else if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"))) {
            return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.OK);
        }
        else return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationDto> updateLocationById(@PathVariable UUID id, @RequestBody LocationDto dto, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException("Location with id " + id + " not found");
        }
        location.setName(dto.getName());
        location.setX(location.getX());
        location.setY(location.getY());

        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());

            if (location.getFirm().equals(admin.getFirm())) {
                location = locationService.updateLocation(dto, id);
                return new ResponseEntity<>(locationMapper.mapToDto(location), HttpStatus.OK);
            }
        }

        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocationById(@PathVariable UUID id, @AuthenticationPrincipal UserDetails userDetails) {
        LocationEntity location = locationService.findById(id);
        if (location == null) {
            throw new ResourceNotFoundException("Location with id " + id + " not found");
        }
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            UserEntity admin = userService.findUserByUsername(userDetails.getUsername());
            if (location.getFirm().equals(admin.getFirm())) {
                locationService.delete(location);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
}
