package kcz.rfid.backend.config.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {
    // development version
    public static final String SECRET = "w4p9Y8Yb6fJk3RbT4aQ2hMnm5G9ZsXvP1tQ8Xk2L3Ug=";


    public String generateToken(String email) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("type", TokenType.USER.toString());
        return createToken(claims,email);
    }

    public String generateDeviceToken(UUID deviceId, UUID companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("deviceId", deviceId.toString());
        claims.put("companyId", companyId.toString());
        claims.put("type", TokenType.DEVICE.toString());

        return createToken(claims, deviceId.toString());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
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

    public UUID extractCompanyId(String token) {
        return UUID.fromString((String) extractAllClaims(token).get("companyId"));
    }

    public String extractTokenType(String token) {
        return (String) extractAllClaims(token).get("type");
    }

    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
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
