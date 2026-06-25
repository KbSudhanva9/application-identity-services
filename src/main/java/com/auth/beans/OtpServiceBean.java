package com.auth.beans;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth.dto.AuthResponse;
import com.auth.dto.OtpDetails;
import com.auth.dto.OtpRequest;
import com.auth.entity.MasterRedirections;
import com.auth.entity.User;
import com.auth.entity.UserSession;
import com.auth.enums.TokenType;
import com.auth.repository.MasterRedirectionsRepository;
import com.auth.repository.UserRepository;
import com.auth.repository.UserSessionRepository;
import com.auth.util.JwtUtil;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class OtpServiceBean {

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from}")
    private String fromEmail;

    // Twilio configurations
    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.sms-sender:}")
    private String smsSender;

    @Value("${twilio.whatsapp-sender:}")
    private String whatsappSender;
    
    @Autowired
    private MasterRedirectionsRepository masterRedirectionsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserSessionRepository userSessionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    // Shared Cache: Stores either the email OR phone number mapped against OTP metadata
    private final Map<String, OtpDetails> otpCache = new ConcurrentHashMap<>();

    public OtpServiceBean(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @PostConstruct
    public void initTwilio() {
        // Initialize Twilio only if credentials are provided in your properties file
        if (accountSid != null && !accountSid.isBlank() && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
        }
    }


//  ----------------login-otp-flow--------------------------------------------------------------------------------
    
    // 1. Generate 5-Digit OTP & Send Email (Kept intact)
    public void sendOtpEmail(String toEmail) throws MessagingException {
        int code = 10000 + secureRandom.nextInt(90000);
        String otp = String.valueOf(code);
        
        otpCache.put(toEmail, new OtpDetails(otp, 3));

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your 3-Minute Verification Code");

        String htmlContent = "<h3>Security Verification</h3>"
                + "<p>Please use the following One-Time Password (OTP) to complete your login:</p>"
                + "<h2 style='color:#e53e3e; letter-spacing: 4px; font-size: 28px;'>" + otp + "</h2>"
                + "<p><strong>Important:</strong> This code is highly time-sensitive and will expire in 3 minutes.</p>";

        helper.setText(htmlContent, true);
        System.out.println("Generated OTP for " + toEmail + ": " + otp);
        mailSender.send(mimeMessage);
    }

    // NEW METHOD: Generate 5-Digit OTP & Send via SMS or WhatsApp
    public void sendOtpMobile(String phoneNumber, String channel) {
        // Enforces exactly a 5-digit number (10000 - 99999)
        int code = 10000 + secureRandom.nextInt(90000);
        String otp = String.valueOf(code);
        System.out.println("Entering sendOtpMobile()");

        // Cache the code against the phone number with the same strict 3-minute lifespan
        otpCache.put(phoneNumber, new OtpDetails(otp, 3));
        System.out.println("Generated OTP for  " + phoneNumber + " via [" + channel + "]: " + otp);

        String smsMessageBody = "Your verification code is: " + otp + ". It will expire in 3 minutes.";

        if ("whatsapp".equalsIgnoreCase(channel)) {
        	  System.out.println(" sendOtpMobile() Channel whatsapp ");
            // WhatsApp requires the 'whatsapp:' prefix on both source and destination numbers
            String formattedTo = phoneNumber.startsWith("whatsapp:") ? phoneNumber : "whatsapp:" + phoneNumber;
            String formattedFrom = whatsappSender.startsWith("whatsapp:") ? whatsappSender : "whatsapp:" + whatsappSender;

            // Note: For live production, this string layout must match your approved Meta/Twilio message templates
            Message.creator(
                new PhoneNumber(formattedTo),
                new PhoneNumber(formattedFrom),
                smsMessageBody
            ).create();
            
        } else { // Fallback / Default channel assumes "sms"
        	System.out.println(" sendOtpMobile() Channel Else SMS ");
            Message.creator(
                new PhoneNumber(phoneNumber), // Needs   +919876543210
                new PhoneNumber(smsSender),
                smsMessageBody
            ).create();
        }
        
        System.out.println("Leaving sendOtpMobile()");
    }
    
    // 2. Validate OTP with Expiration Checks (Universally handles both Email and Phone keys!)
    public AuthResponse validateOtp(String target, String userInputOtp, OtpRequest request) {
    	
    	MasterRedirections callbackUrl = masterRedirectionsRepository.findByRole("CALL_BACK");
    	
        if (!otpCache.containsKey(target)) {
            return null; // No OTP requested for this identifier
        }

        OtpDetails details = otpCache.get(target);

        // Check if the 3-minute window has closed
        if (details.isExpired()) {
            otpCache.remove(target); // Clean up expired data
            return null; // OTP has expired
        }

        // Match user code against cached code
        if (details.getCode().equals(userInputOtp)) {
        	
        	
        	OtpRequest req = request;
        	
        	req.channel();
        	
        	User user;// = null; 
        	MasterRedirections redirectionUrl;// =null;
        	
        	if(req.channel().equalsIgnoreCase("email")) {
        		user = userRepository.findByEmail(req.email()).orElseThrow(() -> new RuntimeException("User not found"));
        		redirectionUrl = masterRedirectionsRepository.findByRole(user.getRole().toString());
        	} else if(req.channel().equalsIgnoreCase("sms") || req.channel().equalsIgnoreCase("whatsapp")) {
        		user = userRepository.findByPhone(req.phone()).orElseThrow(() -> new RuntimeException("User not found"));
        		redirectionUrl = masterRedirectionsRepository.findByRole(user.getRole().toString());
        	} else {
        		throw new RuntimeException("Invalid channel specified");
        	}
        	
        	if(user.isActive() == false) {
            	throw new RuntimeException("User is in-active, Please contact to support.");
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
            
            otpCache.remove(target);

            return new AuthResponse(
                    accessToken,
                    "N/A",
                    sessionId,
                    redirectionUrl.getRedirectUrl(),
                    callbackUrl.getRedirectUrl()
            );
        	
//            otpCache.remove(target); // Invalidate instantly on success to prevent replay attacks
            
//            return new AuthResponse("OTP validated successfully", callbackUrl.getRedirectionUrl(), null, null);
        }

        return null; // OTP mismatch
    }
    

//  ----------------login-otp-flow--------------------------------------------------------------------------------
    
    

//  ----------------reset-password-flow--------------------------------------------------------------------------------

 // 1. Generate 5-Digit OTP & Send Email (Kept intact)
    public void sendResetOtpEmail(String toEmail) throws MessagingException {
        int code = 10000 + secureRandom.nextInt(90000);
        String otp = String.valueOf(code);
        
        otpCache.put(toEmail, new OtpDetails(otp, 3));

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Your 3-Minute Verification Code");

        String htmlContent = "<h3>Security Verification</h3>"
                + "<p>Please use the following One-Time Password (OTP) to reset your Password:</p>"
                + "<h2 style='color:#e53e3e; letter-spacing: 4px; font-size: 28px;'>" + otp + "</h2>"
                + "<p><strong>Important:</strong> This code is highly time-sensitive and will expire in 3 minutes.</p>";
        helper.setText(htmlContent, true);
        System.out.println("Generated OTP for " + toEmail + ": " + otp);
        mailSender.send(mimeMessage);
    }

    // NEW METHOD: Generate 5-Digit OTP & Send via SMS or WhatsApp
    public void sendResetOtpMobile(String phoneNumber, String channel) {
        // Enforces exactly a 5-digit number (10000 - 99999)
        int code = 10000 + secureRandom.nextInt(90000);
        String otp = String.valueOf(code);
        System.out.println("Entering sendOtpMobile()");

        // Cache the code against the phone number with the same strict 3-minute lifespan
        otpCache.put(phoneNumber, new OtpDetails(otp, 3));
        System.out.println("Generated OTP for  " + phoneNumber + " via [" + channel + "]: " + otp);

        String smsMessageBody = "Your password reset code is: " + otp + ". It will expire in 3 minutes.";

        if ("whatsapp".equalsIgnoreCase(channel)) {
        	  System.out.println(" sendOtpMobile() Channel whatsapp ");
            // WhatsApp requires the 'whatsapp:' prefix on both source and destination numbers
            String formattedTo = phoneNumber.startsWith("whatsapp:") ? phoneNumber : "whatsapp:" + phoneNumber;
            String formattedFrom = whatsappSender.startsWith("whatsapp:") ? whatsappSender : "whatsapp:" + whatsappSender;

            // Note: For live production, this string layout must match your approved Meta/Twilio message templates
            Message.creator(
                new PhoneNumber(formattedTo),
                new PhoneNumber(formattedFrom),
                smsMessageBody
            ).create();
            
        } else { // Fallback / Default channel assumes "sms"
        	System.out.println(" sendOtpMobile() Channel Else SMS ");
            Message.creator(
                new PhoneNumber(phoneNumber), // Needs   +919876543210
                new PhoneNumber(smsSender),
                smsMessageBody
            ).create();
        }
        
        System.out.println("Leaving sendOtpMobile()");
	}    
    
    public String resetPasswordWithValidateOtp(String target, String userInputOtp, OtpRequest request) {
    	
        if (!otpCache.containsKey(target)) {
            return "No OTP requested for this identifier"; // No OTP requested for this identifier
        }

        OtpDetails details = otpCache.get(target);

        // Check if the 3-minute window has closed
        if (details.isExpired()) {
            otpCache.remove(target); // Clean up expired data
            return "OTP has expired"; // OTP has expired
        }

        // Match user code against cached code
        if (details.getCode().equals(userInputOtp)) {
        	
        	
        	OtpRequest req = request;
        	
        	req.channel();
        	
        	User user;// = null;         	
        	
        	if(req.channel().equalsIgnoreCase("email")) {
        		user = userRepository.findByEmail(req.email()).orElseThrow(() -> new RuntimeException("User not found"));
        		
        		if(user.isActive() == false) {
                	throw new RuntimeException("User is in-active, Please contact to support.");
                }
        		
        		user.setPassword(passwordEncoder.encode(request.newPassword()));
        		userRepository.save(user);
        		otpCache.remove(target);
                return "OTP verified. Your password change successfull.";
                
//        		redirectionUrl = masterRedirectionsRepository.findByRole(user.getRole().toString());
        	} else if(req.channel().equalsIgnoreCase("sms") || req.channel().equalsIgnoreCase("whatsapp")) {
        		user = userRepository.findByPhone(req.phone()).orElseThrow(() -> new RuntimeException("User not found"));
        		
        		if(user.isActive() == false) {
                	throw new RuntimeException("User is in-active, Please contact to support.");
                }
        		
        		user.setPassword(passwordEncoder.encode(request.newPassword()));
        		userRepository.save(user);
        		otpCache.remove(target);
                return "OTP verified. Your password change successfull.";
                
//        		redirectionUrl = masterRedirectionsRepository.findByRole(user.getRole().toString());
        	} else {
        		throw new RuntimeException("Invalid channel specified");
        	}
        }

        return "OTP mismatch"; // OTP mismatch
    }
    

//  ----------------reset-password-flow--------------------------------------------------------------------------------

	
}
