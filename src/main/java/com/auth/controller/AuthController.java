package com.auth.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.auth.beans.OtpServiceBean;
import com.auth.dto.ApiResponse;
import com.auth.dto.AuthResponse;
import com.auth.dto.LoginRequest;
import com.auth.dto.OtpRequest;
import com.auth.dto.PaginationResponse;
import com.auth.dto.ProfileResponse;
import com.auth.dto.RefreshTokenRequest;
import com.auth.dto.RegisterRequest;
import com.auth.dto.ResetPasswordRequest;
import com.auth.dto.UpdateUserStatus;
import com.auth.dto.UserFilterRequest;
import com.auth.dto.UserResponse;
import com.auth.service.AuthService;




@RestController
@RequestMapping("/auth")

public class AuthController {

	@Autowired
    private   AuthService authService;
	@Autowired
	private  OtpServiceBean otpService;
    
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
        	System.out.println(" Entering login " +request);
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
    
    @GetMapping("/session/exchange")
    public ResponseEntity<ApiResponse<?>> exchange(@RequestParam String sessionId){
    	try {
    		return ResponseEntity.ok( new ApiResponse<>("Session Generated Sussfully", authService.exchangeSession(sessionId)));
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(e.getMessage(), null)
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
    	//	request.setUserId(userId);
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
            PaginationResponse response = authService.getUsersList(request, page, size);
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
          //  request.setUserId(userId);
            String response = authService.resetPassword(request);
            return ResponseEntity.ok(new ApiResponse<>(response, null));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), null));
        }
    }
   /* 
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody OtpRequest otpRequest) {
        try {
        	System.out.println("Received OTP request for email: " + otpRequest.email());
        	if (!authService.isUserExists(otpRequest.email())) {
        		return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User with email " + otpRequest.email() + " does not exist.");
        		
        	}
            otpService.sendOtpEmail(otpRequest.email());
            return ResponseEntity.ok("OTP sent successfully via SMTP.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to deliver mail: " + e.getMessage());
        }
    }
*/
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody OtpRequest otpRequest) {
        try {
            String channel = (otpRequest.channel() != null) ? otpRequest.channel().toLowerCase() : "email";

            if ("email".equals(channel)) {
                String email = otpRequest.email();
                
                if (email == null || email.isBlank()) {
                    return ResponseEntity.badRequest().body("Email field is required for email channel.");
                }
                if (!authService.isUserExists(email)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email does not exist.");
                }
                otpService.sendOtpEmail(email);
                
            } else { // SMS or WhatsApp
                String phone = otpRequest.phoneNumber();
                if (phone == null || phone.isBlank()) {
                    return ResponseEntity.badRequest().body("PhoneNumber field is required for mobile channels.");
                }
                if (!authService.isUserPhoneExists(phone)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with phone number does not exist.");
                }
                otpService.sendOtpMobile(phone, channel);
            }

            return ResponseEntity.ok("OTP sent successfully via " + channel.toUpperCase() + ".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to deliver OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
		String email = otpRequest.email();
		String otp = otpRequest.otp();
    	System.out.println("verifyOtp OTP request for email: " + email + " with OTP: " + otp);
    	if (otpService.validateOtp(email, otp)) {
            return ResponseEntity.ok("User Verified.");
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Invalid or expired code.");
    }
    
    
    
}