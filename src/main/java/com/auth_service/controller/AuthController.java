package com.auth_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth_service.dto.ApiResponse;
import com.auth_service.dto.AuthResponse;
import com.auth_service.dto.LoginRequest;
import com.auth_service.dto.PaginationResponse;
import com.auth_service.dto.ProfileResponse;
import com.auth_service.dto.RefreshTokenRequest;
import com.auth_service.dto.RegisterRequest;
import com.auth_service.dto.ResetPasswordRequest;
import com.auth_service.dto.UpdateUserStatus;
import com.auth_service.dto.UserFilterRequest;
import com.auth_service.dto.UserResponse;
import com.auth_service.service.AuthService;
//import com.auth_service.service.impl.AuthServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    
//    private final MasterRedirectionsRepository masterRedirectionsRepository;
    
//    @GetMapping
//    public List<MasterRedirections> redirections(){
//    	return masterRedirectionsRepository.findAll();
//    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@RequestBody RegisterRequest request) {
        try {
            String response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(response,null)
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null)
                    );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(
                    new ApiResponse<>("Login successful", response )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), null)
                    );
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody RefreshTokenRequest request) {

        try {
            AuthResponse response = authService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(
                    new ApiResponse<>("Token refreshed successfully",response)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), null)
                    );
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(@RequestHeader("Authorization") String token) {
        try {
            ProfileResponse response = authService.getProfile(token);
            return ResponseEntity.ok(new ApiResponse<>("Profile fetched successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
    
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUserStatus(@PathVariable String userId, @RequestBody UpdateUserStatus request){
    	try {
    		request.setUserId(userId);
			String response = authService.updateUserStatus(request);
			return ResponseEntity.ok(new ApiResponse<>(response, null));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), null));
		}
    }
    
    
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getUsersList(

            @RequestParam(defaultValue = "1")
            Integer page,

            @RequestParam(defaultValue = "10")
            Integer size,

            @RequestBody UserFilterRequest request) {

        try {
            PaginationResponse<UserResponse> response = authService.getUsersList(request, page, size);
            return ResponseEntity.ok(new ApiResponse<>("Users fetched successfully", response));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
    
    @PatchMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable String userId,
            @RequestBody ResetPasswordRequest request) {

        try {

            request.setUserId(userId);

            String response = authService.resetPassword(request);

            return ResponseEntity.ok(
                    new ApiResponse<>(response, null)
            );

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
}