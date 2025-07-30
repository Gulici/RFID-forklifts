package kcz.rfid.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/firms")
@RequiredArgsConstructor
@Tag(name = "Firm", description = "Operations related to firm management")
public class FirmController {
    private final FirmService firmService;
    private final FirmMapper firmMapper;
    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Create a new firm", description = "Creates a new firm. Only users with ROOT role can perform this operation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Firm created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FirmEntity.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT role)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<FirmEntity> createFirm(@RequestBody FirmRegisterDto dto) {
        FirmEntity firm = firmService.createFirm(dto);
        return new ResponseEntity<>(firm, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all firms", description = "Returns a list of all firms. Only users with ROOT role can access.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of firms",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FirmDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT role)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping()
    @PreAuthorize("hasRole('ROOT')")
    public ResponseEntity<List<FirmDto>> getAllFirms() {
        Collection<FirmEntity> firms = firmService.getAll();
        return new ResponseEntity<>(firms.stream().map(firmMapper::mapToDto).toList(), HttpStatus.OK);
    }

    @Operation(summary = "Get firm by ID", description = "Returns details of a firm by its ID. Accessible to ROOT users or users of the firm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Firm found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FirmDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT or not user of firm)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Firm not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROOT') or @securityService.isUserOfFirm(authentication.principal, #id)")
    public ResponseEntity<FirmDto> getFirmById(@PathVariable UUID id) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        return ResponseEntity.ok(firmMapper.mapToDto(firm));
    }

    @Operation(summary = "Get users of a firm", description = "Returns list of users belonging to the specified firm. Accessible to ROOT or firm admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDto.class, type = "array"))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT or not admin of firm)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Firm not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}/users")
    @PreAuthorize("hasRole('ROOT') or @securityService.isAdminOfFirm(authentication.principal, #id)")
    public ResponseEntity<List<UserDto>> geFirmUsers(@PathVariable UUID id) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        List<UserEntity> users = userService.findUsersByFirm(firm);
        return ResponseEntity.ok(users.stream().map(userMapper::mapToDto).toList());
    }

    @Operation(summary = "Update firm details", description = "Updates the firm information. Accessible to ROOT or firm admins.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Firm updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FirmDto.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not ROOT or not admin of firm)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Firm not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROOT') or @securityService.isAdminOfFirm(authentication.principal, #id)")
    public ResponseEntity<FirmDto> updateFirm(@PathVariable UUID id, @RequestBody FirmDto dto) {
        FirmEntity firm = firmService.findById(id).orElseThrow(() -> new ResourceNotFoundException("Firm with id " + id + " not found"));
        firm = firmService.updateFirm(firm, dto);
        return ResponseEntity.ok(firmMapper.mapToDto(firm));
    }
}
