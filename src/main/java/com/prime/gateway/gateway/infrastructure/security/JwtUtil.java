package com.prime.gateway.gateway.infrastructure.security;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Service
public class JwtUtil {
    public static final Logger LOGGER = LoggerFactory.getLogger(JwtUtil.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Value("secret")
    private String secretKey;

    @Value("3000000")
    private Long expiration;

    public String getAuthorizationHeader() {
        return ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes())
                .getRequest()
                .getHeader(AUTHORIZATION_HEADER);
    }

    public String getUsername(String jwt) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }

    public String getUserId(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("userId", Long.class).toString();
    }

    public Date getExpiration(String jwt) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody()
                .getExpiration();
    }

    public boolean validateJwtToken(String jwtToken) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(jwtToken);

            return true;
        } catch (SignatureException e) {
            LOGGER.error("Invalid JWT signature ", e);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            LOGGER.error("Expired JWT token ", e);
        } catch (MalformedJwtException e) {
            LOGGER.error("Invalid JWT token ", e);
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT token ", e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("JWT claims are missing ", e);
        }

        return false;
    }

    public String createJwtToken(Authentication authentication) {
        ProgresoUserDetails progresoUserDetails = (ProgresoUserDetails) authentication.getPrincipal();

        Date creationTime = new Date();
        Date expirationTime = new Date(creationTime.getTime() + expiration);


        return Jwts.builder()
                .claim("userId",progresoUserDetails.getUserId())
                .setSubject(progresoUserDetails.getUsername())
                .setIssuedAt(creationTime)
                .setExpiration(expirationTime)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }
}
