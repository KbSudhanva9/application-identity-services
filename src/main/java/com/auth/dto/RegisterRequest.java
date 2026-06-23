package com.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest ( 
		
		@NotBlank
		String name,

		@NotBlank
		String email,

		@NotBlank
		String password,

		@NotBlank
		String role
		
		){
	
}
//import lombok.Data;
//
//@Data
//public class RegisterRequest {
//
//    private String name;
//
//    private String email;
//
//    private String password;
//
//    private String role;
//}
