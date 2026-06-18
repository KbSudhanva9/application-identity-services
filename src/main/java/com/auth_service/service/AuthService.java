package com.auth_service.service;

import com.auth_service.dto.AuthResponse;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.PaginationResponse;
import com.auth_service.dto.ProfileResponse;
import com.auth_service.dto.RegisterRequest;
import com.auth_service.dto.ResetPasswordRequest;
import com.auth_service.dto.UpdateUserStatus;
import com.auth_service.dto.UserFilterRequest;
import com.auth_service.dto.UserResponse;

public interface AuthService {
	
	String register(RegisterRequest request);
	
	AuthResponse login(LoginRequest request);
	
	AuthResponse refreshToken(String refreshToken);
	
	ProfileResponse getProfile(String token);
	
	String updateUserStatus(UpdateUserStatus request);
	
	PaginationResponse<UserResponse> getUsersList(UserFilterRequest request, Integer page, Integer size);
	
	String resetPassword(ResetPasswordRequest request);

}
