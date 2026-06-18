package com.auth_service.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

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
import com.auth_service.entity.UserSession;
import com.auth_service.enums.TokenType;
import com.auth_service.repository.MasterRedirectionsRepository;
import com.auth_service.repository.UserRepository;
import com.auth_service.repository.UserSessionRepository;
import com.auth_service.service.AuthService;
import com.auth_service.specification.UserSpecification;
import com.auth_service.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    
    private final MasterRedirectionsRepository masterRedirectionsRepository;
    
    private final UserSessionRepository userSessionRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;
    
    private enum Role {
    	ADMIN,
    	EDITOR,
    	USER
    };
    
//    private String sessionId = UUID.randomUUID().toString();
    
    private enum OtpType{
    	LOGIN,
    	REGISTER,
    	FORGOT_PASSWORD,
    	EMAIL_VERIFICATION
    }
    
//    private enum sessionType{
//    	SSO_LOGIN,
//    	LOGIN_TOKEN,
//    	REFRESH_TOKEN
//    }

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
                .isActive(true) //added this for default activation
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
                user.getUserId(),
                TokenType.SSO_LOGIN.name()
        );

//        String refreshToken = jwtUtil.generateRefreshToken(
//        		user.getUserId(),
//        		user.getEmail()
//        );
        
        String sessionId = UUID.randomUUID().toString();
        
        UserSession session = new UserSession();
        
        session.setUserId(user.getUserId());
        session.setSessionId(sessionId);
        session.setSessionType(TokenType.SSO_LOGIN.name());
        session.setAccessToken(accessToken);
        session.setExpireTime(LocalDateTime.now().plusMinutes(5));
        session.setCreatedOn(LocalDateTime.now());
        
        userSessionRepository.save(session);

        return new AuthResponse(
                accessToken,
                "N/A",
//                refreshToken,
                sessionId
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
                user.getUserId(),
                TokenType.LOGIN_TOKEN.name()
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(
        		user.getUserId(),
                user.getEmail(),
                TokenType.REFRESH_TOKEN.name()
        );
        
        String sessionId = UUID.randomUUID().toString();
        
        UserSession session = new UserSession();
        
        session.setUserId(user.getUserId());
        session.setSessionId(sessionId);
        session.setSessionType(TokenType.REFRESH_TOKEN.name());
        session.setAccessToken(newAccessToken);
        session.setExpireTime(LocalDateTime.now().plusMinutes(60));
        session.setCreatedOn(LocalDateTime.now());
        
        userSessionRepository.save(session);

        return new AuthResponse(
                newAccessToken,
                newRefreshToken,
                sessionId
        );
        
//    }
        
    }
    
    public ProfileResponse getProfile(String token) {

        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token");
        }

        token = token.substring(7);

        boolean valid = jwtUtil.validateAccessTokenServices(token);
//        		validateAccessToken(token);

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
    
    @Transactional
    public AuthResponse exchangeSession(String sessionId){
    	
//    	UserSession session = userSessionRepository.findBySessionIdAndIsSessionExpiredFalse(sessionId).orElseThrow(
//    							()->new RuntimeException("Invalid session"));
    	
    	UserSession session = validateActiveSession(sessionId);
    	
    	if (!TokenType.SSO_LOGIN.name().equals(session.getSessionType())) {
            throw new RuntimeException("Invalid session type");
        }
    	
//    	if(session.getExpireTime().isBefore(LocalDateTime.now())){
//    	    session.setIsSessionExpired(true);
//    	    userSessionRepository.save(session);
//    	    throw new RuntimeException(
//    	      "Session expired"
//    	    );
//    	}
    	
//    	if(!sessionType.SSO_LOGIN.name().equals(session.getSessionType())) {
//    		throw new RuntimeException("Invalid Token");
//    	}

    	String oldToken=session.getAccessToken();

    	if(!jwtUtil.validateAccessToken(oldToken)){
    	    session.setIsSessionExpired(true);
    	    userSessionRepository.save(session);
    	    throw new RuntimeException(
    	       "Token expired"
    	    );
    	}

//    	User user = userRepository.findById(session.getUserId()).get();
    	
    	User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

    	String newAccessToken = jwtUtil.generateAccessToken(
                user.getEmail(),
                user.getRole(),
                user.getUserId(),
                TokenType.LOGIN_TOKEN.name()
        );

        String newRefreshToken = jwtUtil.generateRefreshToken(
        		user.getUserId(),
                user.getEmail(),
                TokenType.REFRESH_TOKEN.name()
        );

//    	old session
    	session.setIsSessionExpired(true);
    	userSessionRepository.save(session);
        
//    	new session
    	UserSession newSession = new UserSession();
    	
    	newSession.setUserId(user.getUserId());
    	newSession.setSessionId(UUID.randomUUID().toString());
    	newSession.setSessionType(TokenType.LOGIN_TOKEN.name());
    	newSession.setAccessToken(newAccessToken);
    	newSession.setExpireTime(LocalDateTime.now().plusMinutes(60));
    	newSession.setCreatedOn(LocalDateTime.now());
        
        userSessionRepository.save(newSession);

    	return new AuthResponse(newAccessToken, newRefreshToken, null);
    }
    
    private UserSession validateActiveSession(String sessionId) {

        UserSession session = userSessionRepository
                .findBySessionIdAndIsSessionExpiredFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("Invalid session"));

        if (session.getExpireTime().isBefore(LocalDateTime.now())) {
            session.setIsSessionExpired(true);
            userSessionRepository.save(session);
            throw new RuntimeException("Session expired");
        }

        return session;
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