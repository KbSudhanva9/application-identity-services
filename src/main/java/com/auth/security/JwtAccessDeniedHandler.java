package com.auth.security;

import com.auth.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component

public class JwtAccessDeniedHandler
        implements AccessDeniedHandler {

	@Autowired
    private   ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        response.setContentType("application/json");

        ApiResponse<?> apiResponse =
                new ApiResponse<>(
                        "Access denied",
                        403
                );

        response.getWriter().write(
                objectMapper.writeValueAsString(apiResponse)
        );
    }
}