package com.auth.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth.entity.UserSession;
import com.auth.enums.TokenType;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenExpiration;
    
    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenExpiration;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

//    public String generateToken(String email, String role) {
//
//        return Jwts.builder()
//                .subject(email)
//                .claim("role", role)
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + expiration))
//                .signWith(getKey())
//                .compact();
//    }
    
    public String generateAccessToken(String email, String role, String userId, String tokenType) {

        return Jwts.builder()
        		.subject(userId)
//                .subject(email)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .issuer("APPLICATION-IDENTITY")
                .claim("type", tokenType)//"accessToken")
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(String userId, String email, String tokenType) {

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(new Date())
                .issuer("APPLICATION-IDENTITY")
                .claim("type", tokenType)//"refreshToken")
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(getKey())
                .compact();
    }

    public String extractEmail(String token) {
    	return extractClaims(token).get("email").toString();
//    			getSubject();
//        return Jwts.parser()
//                .verifyWith(getKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload()
//                .getSubject();
    }
    
    public Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String extractTokenType(String token) {
        Claims claims = extractClaims(token);
        return claims.get("type", String.class);
    }
    
    public boolean validateRefreshToken(String token) {
    	try {
    		Claims claims = extractClaims(token);
    		String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
    		return "REFRESH_TOKEN".equals(type)
            		&& expiration != null
                    && expiration.after(new Date());
    	} catch (Exception e) {
			return false;
		}
    }

//    public boolean validateToken(String token) {
//        try {
//
//            Jwts.parser()
//                    .verifyWith(getKey())
//                    .build()
//                    .parseSignedClaims(token);
//
//            return true;
//
//        } catch (Exception e) {
//
//            return false;
//        }
//    }
    
    public boolean validateAccessToken(String token) {
        try {
            Claims claims = extractClaims(token);
//            String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
            return //"accessToken".equals(type) && 
            		expiration != null
                    && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
//    validating access token from token type
    public boolean validateAccessTokenServices(String token) {
        try {
            Claims claims = extractClaims(token);
            String type = claims.get("type", String.class);
            Date expiration = claims.getExpiration();
            return "LOGIN_TOKEN".equals(type)
//            		("LOGIN_TOKEN".equals(type) || "REFRESH_TOKEN".equals(type))
            		&& expiration != null
                    && expiration.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
    
}