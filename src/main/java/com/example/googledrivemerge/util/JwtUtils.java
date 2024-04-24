package com.example.googledrivemerge.util;

import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.pojo.MyUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${secret}")
    private String jwtSecret;
    @Value("${expiration}")
    private long jwtExpirationMs; // 1 час

    public String generateJwtToken(Authentication authentication) {

        var user = (MyUserDetails) authentication.getPrincipal();
        return Jwts.builder().setSubject((user.getUsername())).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512).compact();
    }

    public boolean validateJwtToken(String jwt) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(jwtSecret.getBytes())
                    .build()
                    .parseClaimsJws(jwt);
            return true;
        } catch (MalformedJwtException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }

        return false;
    }

    public String getUserNameFromJwtToken(String jwt) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret.getBytes())
                .build()
                .parseClaimsJws(jwt).getBody().getSubject();
    }
}

