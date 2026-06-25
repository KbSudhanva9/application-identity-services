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
import com.auth.dto.ForceResetPasswordRequest;
import com.auth.dto.LoginRequest;
import com.auth.dto.OtpRequest;
import com.auth.dto.PaginationResponse;
import com.auth.dto.ProfileResponse;
import com.auth.dto.RefreshTokenRequest;
import com.auth.dto.RegisterRequest;
import com.auth.dto.UpdateUserStatus;
import com.auth.dto.UserFilterRequest;
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
                    .body(new ApiResponse<>(response,"N/A", "N/A", "N/A")
                    );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A")
                    );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {
        try {
        	System.out.println(" Entering login " +request);
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(
                    new ApiResponse<>("Login successful", "N/A", response, "N/A" )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A")
                    );
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody RefreshTokenRequest request) {

        try {
            AuthResponse response = authService.refreshToken(request.refreshToken());
            return ResponseEntity.ok(
                    new ApiResponse<>("Token refreshed successfully", "N/A", response, "N/A")
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A")
                    );
        }
    }
    
    @GetMapping("/session/exchange")
    public ResponseEntity<ApiResponse<?>> exchange(@RequestParam String sessionId){
    	try {
    		return ResponseEntity.ok( new ApiResponse<>("Session Generated Sussfully", "N/A", authService.exchangeSession(sessionId), "N/A"));
    	} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A")
                    );
        }
    }
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<?>> getProfile(@RequestHeader("Authorization") String token) {
        try {
            ProfileResponse response = authService.getProfile(token);
            return ResponseEntity.ok(new ApiResponse<>("Profile fetched successfully", response, "N/A", "N/A"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A"));
        }
    }
    
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> updateUserStatus(@PathVariable String userId, @RequestBody UpdateUserStatus request){
    	try {
    	//	request.setUserId(userId);
			String response = authService.updateUserStatus(request, String.valueOf(userId));
			return ResponseEntity.ok(new ApiResponse<>(response, "N/A", "N/A", "N/A"));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A"));
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
            return ResponseEntity.ok(new ApiResponse<>("Users fetched successfully", response, "N/A", "N/A"));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A"));
        }
    }

    @PatchMapping("/{userId}/force-reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable String userId,
            @RequestBody ForceResetPasswordRequest request) {

        try {
          //  request.setUserId(userId);
            String response = authService.forceResetPassword(request);
            return ResponseEntity.ok(new ApiResponse<>(response, "N/A", "N/A", "N/A"));

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A"));
        }
    }
    
//    @PatchMapping("/forget-password")
//    public ResponseEntity<ApiResponse<?>> forgetPassword(
////            @PathVariable String userId,
//            @RequestBody ResetPasswordRequest request) {
//
//        try {
//          //  request.setUserId(userId);
//            String response = authService.resetPassword(request);
//            return ResponseEntity.ok(new ApiResponse<>(response, null));
//
//        } catch (Exception e) {
//
//            return ResponseEntity.badRequest()
//                    .body(new ApiResponse<>(e.getMessage(), null));
//        }
//    }
    
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
    public ResponseEntity<ApiResponse<?>> requestOtp(@RequestBody OtpRequest otpRequest) {
        try {
            String channel = (otpRequest.channel() != null) ? otpRequest.channel().toLowerCase() : "email";
            System.out.println("requestOtp" +otpRequest );
            if ("email".equals(channel)) {
                String email = otpRequest.email();
                
                if (email == null || email.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>("Email field is required for email channel.", "N/A", "N/A", "N/A"));
//                    		body("Email field is required for email channel.");
                }
                if (!authService.isUserExists(email)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with email does not exist.", "N/A", "N/A", "N/A"));
//                    		body("User with email does not exist.");
                }
                otpService.sendOtpEmail(email);
                
            } else { // SMS or WhatsApp
                String phone = otpRequest.phone();
                if (phone == null || phone.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>("PhoneNumber field is required for mobile channels.", "N/A", "N/A", "N/A"));
//                    		body("PhoneNumber field is required for mobile channels.");
                }
                if (!authService.isUserPhoneExists(phone)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with phone number does not exist.", "N/A", "N/A", "N/A"));
//                    		body("User with phone number does not exist.");
                }
                otpService.sendOtpMobile(phone, channel);
            }

            return ResponseEntity.ok(new ApiResponse<>("OTP sent successfully via " + channel.toUpperCase() + ".", "N/A", "N/A", "N/A"));
//            		("OTP sent successfully via " + channel.toUpperCase() + ".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("Failed to deliver OTP: " + e.getMessage(), "N/A", "N/A", "N/A"));
//            		body("Failed to deliver OTP: " + e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse<?>> verifyOtp(@RequestBody OtpRequest otpRequest) {
    	System.out.println("Entering  verifyOtp " +otpRequest);
    	//String channel = (otpRequest.channel() != null) ? otpRequest.channel().toLowerCase() : "email";
		String target = (otpRequest.email() == null) ? otpRequest.phone():otpRequest.email();
		String otp = otpRequest.otp();
	
    	System.out.println("verifyOtp OTP request : " + target + " with OTP: " + otp);
    	
    	AuthResponse response = otpService.validateOtp(target, otp, otpRequest);
    	
//    	if (otpService.validateOtp(target, otp, otpRequest)) {
    	if(!(response==null)) {
    		
            return ResponseEntity.ok(new ApiResponse<>("User Verified.", "N/A", response, "N/A"));
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiResponse<>("Invalid or expired code.", "N/A", "N/A", "N/A"));

    }
    
    
    
    @PostMapping("/valid-user")
    public ResponseEntity<ApiResponse<?>> isValidUser(@RequestBody OtpRequest otpRequest) {
		String target = (otpRequest.email() == null) ? otpRequest.phone() : otpRequest.email();
		String channel = (otpRequest.channel() != null) ? otpRequest.channel().toLowerCase() : "email";
		if(target==null || target.isBlank() || channel==null || channel.isBlank()) {
			return ResponseEntity.badRequest().body(new ApiResponse<>("Target and channel fields are required.", "N/A", "N/A", "error"));
		}else if (channel.equals("email")) {
			if (!authService.isUserExists(otpRequest.email())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with email does not exist.", "N/A", "N/A", "error"));
			} else {
				return ResponseEntity.ok(new ApiResponse<>("User email existence check completed.", true, "N/A", "N/A"));
			}
//			return ResponseEntity.ok(new ApiResponse<>("User email existence check completed.", authService.isUserExists(otpRequest.email()),"N/A", "N/A"));
		} else if (channel.equals("sms") || channel.equals("whatsapp")) {
			if (!authService.isUserPhoneExists(otpRequest.phone())) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with phone number does not exist.", "N/A", "N/A", "error"));
			} else {
				return ResponseEntity.ok(new ApiResponse<>("User phone existence check completed.", true, "N/A", "N/A"));
			}
//			return ResponseEntity.ok(new ApiResponse<>("User phone existence check completed.", authService.isUserPhoneExists(otpRequest.phone()),"N/A", "N/A"));
		} else {
			return ResponseEntity.badRequest().body(new ApiResponse<>("Invalid channel. Must be 'email', 'sms', or 'whatsapp'.", "N/A", "N/A", "error"));
		}
	}
    
    
//    -------------------reset password with otp-------------------
    
    @PostMapping("/reset-password-otp")
    public ResponseEntity<ApiResponse<?>> resetPasswordOtp(@RequestBody OtpRequest otpRequest) {
        try {
            String channel = (otpRequest.channel() != null) ? otpRequest.channel().toLowerCase() : "email";
            System.out.println("requestOtp" +otpRequest );
            if ("email".equals(channel)) {
                String email = otpRequest.email();
                
                if (email == null || email.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>("Email field is required for email channel.", "N/A", "N/A", "N/A"));
//                    		body("Email field is required for email channel.");
                }
                if (!authService.isUserExists(email)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with email does not exist.", "N/A", "N/A", "N/A"));
//                    		body("User with email does not exist.");
                }
                otpService.sendResetOtpEmail(email);
                
            } else { // SMS or WhatsApp
                String phone = otpRequest.phone();
                if (phone == null || phone.isBlank()) {
                    return ResponseEntity.badRequest().body(new ApiResponse<>("PhoneNumber field is required for mobile channels.", "N/A", "N/A", "N/A"));
//                    		body("PhoneNumber field is required for mobile channels.");
                }
                if (!authService.isUserPhoneExists(phone)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>("User with phone number does not exist.", "N/A", "N/A", "N/A"));
//                    		body("User with phone number does not exist.");
                }
                otpService.sendResetOtpMobile(phone, channel);
            }

            return ResponseEntity.ok(new ApiResponse<>("Password reset OTP sent successfully via " + channel.toUpperCase() + ".", "N/A", "N/A", "N/A"));
//            		("OTP sent successfully via " + channel.toUpperCase() + ".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>("Failed to deliver OTP: " + e.getMessage(), "N/A", "N/A", "N/A"));
//            		body("Failed to deliver OTP: " + e.getMessage());
        }
    }
    
    @PatchMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody OtpRequest request) {
    	
    	String target = (request.email() == null) ? request.phone():request.email();
		String otp = request.otp();

        try {
          //  request.setUserId(userId);
            String response = otpService.resetPasswordWithValidateOtp(target, otp, request);
//            		authService.resetPassword(request);
//            return ResponseEntity.ok(new ApiResponse<>(response, "N/A", "N/A", "N/A"));
            
            if(response.contains("OTP verified")) {
				return ResponseEntity.ok(new ApiResponse<>(response, "N/A", "N/A", "N/A"));
			}else {
				return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ApiResponse<>(response, "N/A", "N/A", "N/A"));
			}

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(e.getMessage(), "N/A", "N/A", "N/A"));
        }
    }
    

//  -------------------reset password with otp-------------------
    
    
}


