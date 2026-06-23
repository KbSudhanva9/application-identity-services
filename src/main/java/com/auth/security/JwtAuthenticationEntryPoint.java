package com.auth.security;

import com.auth.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
 
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

	@Autowired
    private   ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.setContentType("application/json");

        ApiResponse<?> apiResponse =
                new ApiResponse<>(
                        "Unauthorized access",
                        401
                );

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}