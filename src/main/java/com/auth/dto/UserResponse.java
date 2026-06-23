package com.auth.dto;

import java.time.LocalDateTime;

public record UserResponse(
		String userId,
	    String name,
	    String email,
	    String role,
	    String phone,
	    LocalDateTime createdAt,
	    boolean isActive
		) {
	
}

//import lombok.*;
//
//import java.time.LocalDateTime;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class UserResponse {
//
//    private String userId;
//
//    private String name;
//
//    private String email;
//
//    private String role;
//
//    private LocalDateTime createdAt;
//
//    private boolean isActive;
//}