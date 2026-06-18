package com.auth_service.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank
		String email,
		
		@NotBlank
		String password) {
	
}

//import lombok.Data;
//
//@Data
//public class LoginRequest {
//
//    private String email;
//    private String password;
//}
