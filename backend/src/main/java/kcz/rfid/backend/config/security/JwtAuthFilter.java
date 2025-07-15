package kcz.rfid.backend.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kcz.rfid.backend.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService, UserService userService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            String type = jwtService.extractTokenType(token);

            if ("device".equals(type)) {
                handleDeviceToken(token);
            } else if ("user".equals(type)) {
                handleUserToken(token);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        filterChain.doFilter(request, response);
    }

    private void handleUserToken(String token) {
        String username = jwtService.extractUsername(token);
        UserDetails userDetails = userService.loadUserByUsername(username);
        if (jwtService.validateUserToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    private void handleDeviceToken(String token) {
        UUID deviceId = jwtService.extractDeviceId(token);
        UUID firmId = jwtService.extractCompanyId(token);
        if (jwtService.validateDeviceToken(token, deviceId)) {
            DeviceAuthenticationToken authToken = new DeviceAuthenticationToken(deviceId, firmId);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

    }
}
