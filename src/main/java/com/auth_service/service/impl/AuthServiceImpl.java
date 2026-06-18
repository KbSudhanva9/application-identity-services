package com.auth_service.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth_service.dto.AuthResponse;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.PaginationResponse;
import com.auth_service.dto.ProfileResponse;
import com.auth_service.dto.RegisterRequest;
import com.auth_service.dto.ResetPasswordRequest;
import com.auth_service.dto.UpdateUserStatus;
import com.auth_service.dto.UserFilterRequest;
import com.auth_service.dto.UserResponse;
import com.auth_service.entity.MasterRedirections;
import com.auth_service.entity.User;
import com.auth_service.repository.MasterRedirectionsRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.service.AuthService;
import com.auth_service.specification.UserSpecification;
import com.auth_service.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    
    private final MasterRedirectionsRepository masterRedirectionsRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    
    private enum Role {
    	ADMIN,
    	EDITOR,
    	USER
    };
    
//    private enum Role {
//		BUYER,
//		SELLER,
//		BUYER_SELLER
//	}
    
    private enum Otp_Type{
    	LOGIN,
    	REGISTER,
    	FORGOT_PASSWORD,
    	EMAIL_VERIFICATION
    }

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }
        
        boolean validRole = java.util.Arrays.stream(Role.values())
				.anyMatch(role -> role.toString().equalsIgnoreCase(request.role()));
        
        if (!validRole) {
        	throw new RuntimeException(
        	        "Invalid role. Please select one of the following: " +
        	        java.util.Arrays.toString(Role.values())
        	    );
		}

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role().toUpperCase()).toString())	
                .build();

        userRepository.save(user);

        return "User registered successfully";
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.isActive() == false) {
        	throw new RuntimeException("User is in-active, Please contact to support.");
        }
        
        boolean matches = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );

        if (!matches) {
            throw new RuntimeException("Invalid password");
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole(),
                user.getUserId()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
        		user.getUserId(),
        		user.getEmail()
        );

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }
    
    public AuthResponse refreshToken(String refreshToken) {

        boolean valid = jwtUtil.validateRefreshToken(refreshToken);

        if (!valid) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole(),
                user.getUserId()
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(
        		user.getUserId(),
                user.getEmail()
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken
        );
    }
    
    public ProfileResponse getProfile(String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }

        token = token.substring(7);

        boolean valid = jwtUtil.validateAccessToken(token);

        if (!valid) {
            throw new RuntimeException("Invalid access token");
        }

        String email = jwtUtil.extractEmail(token);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        MasterRedirections redirectionUrl = masterRedirectionsRepository.findByRole(
        		user.getRole().toString());

        return ProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .redirectUrl(redirectionUrl.getRedirectUrl())
                .callbackUrl("http://localhost:5173/")
                .build();
    }
    
    public String updateUserStatus(UpdateUserStatus request) {
    	User user = userRepository
    					.findByUserId(request.getUserId())
    					.orElseThrow(() -> new RuntimeException("User not found..")
    							);
    	
    	user.setActive(request.getIsActive());
    	userRepository.save(user);
    	
    	return "User status updated successfully";
    }
    
    public PaginationResponse<UserResponse> getUsersList(UserFilterRequest request, Integer page, Integer size) {

        Pageable pageable = PageRequest.of(
        		(page == null || page < 1) ? 0 : page - 1,
        		(size == null || size < 1) ? 10 : size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<User> users = userRepository.findAll(
                UserSpecification.filterUsers(request),
                pageable
        );

        Page<UserResponse> responsePage =
                users.map(user -> new UserResponse(
                        user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.getCreatedAt(),
                        user.isActive()
                ));
        
//        Page<UserResponse> responsePage =
//                users.map(user -> UserResponse.builder()
//                        .userId(user.getUserId())
//                        .name(user.getName())
//                        .email(user.getEmail())
//                        .role(user.getRole())
//                        .createdAt(user.getCreatedAt())
//                        .isActive(user.isActive())
//                        .build()
//                );

        return PaginationResponse.<UserResponse>builder()
                .content(responsePage.getContent())
                .page(responsePage.getNumber()+1)
                .size(responsePage.getSize())
                .totalElements(responsePage.getTotalElements())
                .totalPages(responsePage.getTotalPages())
                .last(responsePage.isLast())
                .build();
    }
    
    
    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository
                .findByUserId(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return "Force password reset successfully";
    }
}