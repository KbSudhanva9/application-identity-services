package com.auth.service;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.PaginationResponse;
import com.auth.dto.ProfileResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.ResetPasswordRequest;
import com.auth.dto.UpdateUserStatus;
import com.auth.dto.UserFilterRequest;
import com.auth.dto.UserResponse;

public interface AuthService {
	
	public	String register(RegisterRequest request);
	
	public	AuthResponse login(LoginRequest request);
	
	public	AuthResponse refreshToken(String refreshToken);
	
	public	ProfileResponse getProfile(String token);
	
	public	String updateUserStatus(UpdateUserStatus request);
	
	public	String resetPassword(ResetPasswordRequest request);

	public	AuthResponse exchangeSession(String session);
	
	public PaginationResponse getUsersList(UserFilterRequest request, Integer page, Integer size);

	public	boolean isUserExists(String email);
//	
	public boolean isUserPhoneExists(String phone);

}
