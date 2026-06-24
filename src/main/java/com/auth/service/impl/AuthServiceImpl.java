package com.auth.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.PaginationResponse;
import com.auth.dto.ProfileResponse;
import com.auth.dto.RegisterRequest;
import com.auth.dto.ResetPasswordRequest;
import com.auth.dto.UpdateUserStatus;
import com.auth.dto.UserFilterRequest;
import com.auth.dto.UserResponse;
import com.auth.entity.MasterRedirections;
import com.auth.entity.User;
import com.auth.entity.UserSession;
import com.auth.enums.TokenType;
import com.auth.repository.MasterRedirectionsRepository;
import com.auth.repository.UserRepository;
import com.auth.repository.UserSessionRepository;
import com.auth.service.AuthService;
import com.auth.specification.UserSpecification;
import com.auth.util.JwtUtil;

import jakarta.transaction.Transactional;


@Service

public class AuthServiceImpl implements AuthService {

	@Autowired
    private   UserRepository userRepository;
	@Autowired
    private   MasterRedirectionsRepository masterRedirectionsRepository;
	@Autowired
    private   UserSessionRepository userSessionRepository;

    @Autowired
    private   PasswordEncoder passwordEncoder;

    @Autowired
    private   JwtUtil jwtUtil;
    
    private enum Role {
    	ADMIN,
    	EDITOR,
    	USER
    };
    
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
        
        if (userRepository.existsByPhone(request.phone())) {
        	throw new RuntimeException("Phone already exists with another user account");
        }
        
//        boolean validRole = java.util.Arrays.stream(Role.values())
//				.anyMatch(role -> role.toString().equalsIgnoreCase(request.role()));
        
//        if (!validRole) {
//        	throw new RuntimeException(
//        	        "Invalid role. Please select one of the following: " +
//        	        java.util.Arrays.toString(Role.values())
//        	    );
//		}

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.valueOf(request.role().toUpperCase()).toString())	;
        user.setPhone(request.phone());
        user.setActive(true) ; 
          

        userRepository.save(user);

        return "User registered successfully";
    }

    
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        MasterRedirections redirectionUrl = masterRedirectionsRepository.findByRole(user.getRole().toString());
        
        MasterRedirections callbackUrl = masterRedirectionsRepository.findByRole("CALL_BACK");
//        		findByRole(user.getRole().toString());

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
                sessionId,
                redirectionUrl.getRedirectUrl(),
                callbackUrl.getRedirectUrl()
//                "http://localhost:5173/"
        );
    }
    
    
    public AuthResponse refreshToken(String refreshToken) {

        boolean valid = jwtUtil.validateRefreshToken(refreshToken);
        

        MasterRedirections callbackUrl = masterRedirectionsRepository.findByRole("CALL_BACK");

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
                sessionId,
                null,
                callbackUrl.getRedirectUrl()
        );        
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

        return new ProfileResponse(user.getUserId(),user.getName(),user.getEmail(),user.getRole(),user.getPhone());
    }
    
    @Transactional
    public AuthResponse exchangeSession(String sessionId){
    	
    	UserSession session = validateActiveSession(sessionId);
    	
    	if (!TokenType.SSO_LOGIN.name().equals(session.getSessionType())) {
            throw new RuntimeException("Invalid session type");
        }

    	String oldToken=session.getAccessToken();

    	if(!jwtUtil.validateAccessToken(oldToken)){
    	    session.setIsSessionExpired(true);
    	    userSessionRepository.save(session);
    	    throw new RuntimeException(
    	       "Token expired"
    	    );
    	}
    	
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

    	return new AuthResponse(newAccessToken, newRefreshToken, null,null,null);
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
    
    
    
    
    public String updateUserStatus(UpdateUserStatus request, String userId) {
    	
    	User user = userRepository
    					.findById(userId)
//    					.findByUserId(request.userId())
    					.orElseThrow(() -> new RuntimeException("User not found..")
    							);
    	
    	user.setActive(request.isActive());
    	userRepository.save(user);
    	
    	return "User status updated successfully";
    }
    
    
    public PaginationResponse getUsersList(UserFilterRequest request, Integer page, Integer size) {

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
                        user.getPhone(),
                        user.getCreatedAt(),
                        user.isActive()
                ));

        return new PaginationResponse(responsePage.getContent(),(responsePage.getNumber()+1),responsePage.getSize(),responsePage.getTotalElements(),responsePage.getTotalPages(),responsePage.isLast());
                
    }
    
    
    public String resetPassword(ResetPasswordRequest request) {

        User user = userRepository
                .findByUserId(request.userId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        return "Force password reset successfully";
    }

//	@Override
	public boolean  isUserExists(String email) {
		return userRepository.existsByEmail(email);
		
	}


//	@Override
	public boolean isUserPhoneExists(String phone) {
		// TODO Auto-generated method stub
		  return userRepository.existsByPhone(phone);
	}

}