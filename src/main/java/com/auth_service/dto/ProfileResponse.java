package com.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {

    private String userId;

    private String name;

    private String email;

    private String role;
    
    private String redirectUrl;
    
    private String callbackUrl;
}