package com.auth_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    private String userId;

    private String name;

    private String email;

    private String role;

    private Double usedDeposit;

    private Double deposit;

    private LocalDateTime createdAt;

    private boolean isActive;
}