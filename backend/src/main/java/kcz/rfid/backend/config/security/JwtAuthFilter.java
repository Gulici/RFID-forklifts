package kcz.rfid.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kcz.rfid.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            switch (jwtService.extractTokenType(token)) {
                case "USER" -> handleUserToken(token);
                case "DEVICE" -> handleDeviceToken(token);
                default -> logger.warn("Unsupported token type: " + token);
            }
        } catch (Exception e) {
            logger.error("JWT filter error: " + e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private void handleUserToken(String token) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userService.loadUserByUsername(username);

        if (jwtService.validateUserToken(token, userDetails)) {

            logger.info(String.format("User %s logged in", userDetails.getUsername()));
            logger.info(String.format("User %s authorities in", userDetails.getAuthorities()));

            var auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }

    private void handleDeviceToken(String token) {
        UUID deviceId = jwtService.extractDeviceId(token);
        UUID firmId = jwtService.extractCompanyId(token);

        if (jwtService.validateDeviceToken(token, deviceId)) {
            var auth = new DeviceAuthenticationToken(deviceId, firmId);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
}
