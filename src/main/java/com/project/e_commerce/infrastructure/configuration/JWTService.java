package com.project.e_commerce.infrastructure.configuration;

import com.project.e_commerce.infrastructure.persistence.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JWTService {
    private final SecretKey KEY;

    public JWTService() {
        String secret = System.getenv("SECRET_KEY");
        this.KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userEmail, Set<Role> roles){
        Instant now = Instant.now();
        return Jwts.builder().subject(userEmail).claim("type","access").claim("roles",roles.stream().map(Enum::name).collect(Collectors.toList())).issuedAt(Date.from(now)).expiration(Date.from(now.plus(1, ChronoUnit.HOURS))).signWith(KEY).compact();
    }

    public String generateRefreshToken(String userEmail){
        Instant now = Instant.now();
        return Jwts.builder().subject(userEmail).claim("type","refresh").issuedAt(Date.from(now)).expiration(Date.from(now.plus(7, ChronoUnit.DAYS))).signWith(KEY).compact();
    }

    public String extractUserEmail(String token){
        return Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public List<String> extractRoles(String token){
        Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
        return claims.get("roles", List.class);
    }

    public boolean isValid(String token){
        try {
            Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token);
            return true;
        }catch (JwtException e){
            return false;
        }
    }

    public boolean isRefreshtoken(String token){
        try {
            Claims claims = Jwts.parser().verifyWith(KEY).build().parseSignedClaims(token).getPayload();
            return claims.get("type").equals("refresh");
        }catch (JwtException e){
            return false;
        }
    }
}
