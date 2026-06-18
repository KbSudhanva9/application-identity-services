package com.auth_service.dto;

public record AuthResponse(
		String accessToken,
		String refreshToken) {
}

//import lombok.AllArgsConstructor;
//import lombok.Data;
//
//@Data
//@AllArgsConstructor
//public class AuthResponse {
//
//    private String accessToken;
//    
//    private String refreshToken;
//    
//}
