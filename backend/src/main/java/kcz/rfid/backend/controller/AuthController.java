package kcz.rfid.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kcz.rfid.backend.config.security.JwtService;
import kcz.rfid.backend.model.dto.*;
import kcz.rfid.backend.model.entity.DeviceEntity;
import kcz.rfid.backend.service.DeviceAuthService;
import kcz.rfid.backend.service.DeviceService;
import kcz.rfid.backend.service.NonceService;
import kcz.rfid.backend.service.utils.PemUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Operations related to authentication of users and devices")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final DeviceService deviceService;
    private final NonceService nonceService;
    private final DeviceAuthService deviceAuthService;

    @Operation(summary = "Authenticate user", description = "Authenticates user using username and password, returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authenticated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtDto> login(@RequestBody LoginRequest dto) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
            String jwt = jwtService.generateToken(auth);
            return ResponseEntity.ok(new JwtDto(jwt));

        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Operation(summary = "Authenticate device", description = "Verifies signed nonce from device and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device authenticated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid device credentials or signature",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/verify")
    public ResponseEntity<JwtDto> loginDevice(@RequestBody SignedNonceRequest request) {
        String jwt = deviceAuthService.verifyAndIssueToken(request);
        return ResponseEntity.ok(new JwtDto(jwt));
    }

    @Operation(summary = "Request nonce", description = "Requests a nonce for device authentication using public key PEM")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Nonce generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NonceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Device not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping("/request-nonce")
    public ResponseEntity<NonceResponse> nonceRequest(@RequestBody NonceRequest request) {
        String fingerprint = PemUtils.computeFingerprint(request.getPublicKeyPem());
        DeviceEntity device = deviceService.findDeviceByFingerprint(fingerprint);
        if (device == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Device not found");
        }
        String nonce = nonceService.generateAndSaveNonce(device.getId());
        return ResponseEntity.ok(new NonceResponse(nonce));
    }
}
