package com.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import jakarta.validation.constraints.AssertFalse.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth.enums.TokenType;
import com.auth.util.JwtUtil;

import java.io.IOException;
import java.util.*;

@Component

public class JwtAuthenticationFilter extends OncePerRequestFilter {

	@Autowired
    private  JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        
        String tokenType = jwtUtil.extractTokenType(token);
        
//        if(!tokenType.equals(TokenType.LOGIN_TOKEN.name())){
////        	System.out.println(tokenType);
//            throw new RuntimeException(
//                    "Invalid token type"
//            );
//        }

        boolean valid = jwtUtil.validateAccessToken(token);

        if (valid) {
        	
        	Claims claims = jwtUtil.extractClaims(token);

            String email = jwtUtil.extractEmail(token);
            String role = claims.get("role", String.class);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            
        }

        filterChain.doFilter(request, response);
    }
}