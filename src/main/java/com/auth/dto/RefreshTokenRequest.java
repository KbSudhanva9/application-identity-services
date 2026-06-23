package com.auth.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshTokenRequest(
		@NotNull
		String refreshToken) {}

//import lombok.Data;
//
//@Data
//public class RefreshTokenRequest {
//
//    private String refreshToken;
//}