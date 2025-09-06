package kcz.rfid.backend.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kcz.rfid.backend.model.entity.FirmEntity;
import kcz.rfid.backend.model.entity.UserEntity;
import kcz.rfid.backend.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {
    // development version
    public static final String SECRET = "w4p9Y8Yb6fJk3RbT4aQ2hMnm5G9ZsXvP1tQ8Xk2L3Ug=";
    private final UserService userService;

    public JwtService(UserService userService) {
        this.userService = userService;
    }


    public String generateToken(Authentication auth) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        UserEntity user = userService.findUserByUsername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenType.USER.toString());
        claims.put("firmName", user.getFirm() != null ? user.getFirm().getFirmName() : "ROOT");
        claims.put("roles", roles);

        return createToken(claims,userDetails.getUsername());
    }

    public String generateDeviceToken(UUID deviceId, UUID companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId.toString());
        claims.put("firmId", companyId.toString());
        claims.put("type", TokenType.DEVICE.toString());

        return createToken(claims, deviceId.toString());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(3600 * 24);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public UUID extractDeviceId(String token) {
        return UUID.fromString((String) extractAllClaims(token).get("deviceId"));
    }

    public UUID extractFirmId(String token) {
        return UUID.fromString((String) extractAllClaims(token).get("firmId"));
    }

    public String extractTokenType(String token) {
        return (String) extractAllClaims(token).get("type");
    }

    private boolean isTokenExpired(String token) {
        Instant expiration = extractExpiration(token).toInstant();
        return Instant.now().isAfter(expiration);
    }

    public Boolean validateUserToken(String token, UserDetails userDetails) {
        final String username = this.extractUsername(token);
        return extractTokenType(token).equals(TokenType.USER.toString())
                && username.equals(userDetails.getUsername())
                && isTokenExpired(token);
    }

    public boolean validateDeviceToken(String token, UUID expectedDeviceId) {
        return extractTokenType(token).equals(TokenType.DEVICE.toString())
                && extractDeviceId(token).equals(expectedDeviceId)
                && isTokenExpired(token);
    }
}
