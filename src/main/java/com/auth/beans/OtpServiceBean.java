package com.auth.beans;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.auth.dto.OtpDetails;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        
        // Cache the code against the phone number with the same strict 3-minute lifespan
        otpCache.put(phoneNumber, new OtpDetails(otp, 3));
        System.out.println("Generated OTP for " + phoneNumber + " via [" + channel + "]: " + otp);

        String smsMessageBody = "Your verification code is: " + otp + ". It will expire in 3 minutes.";

        if ("whatsapp".equalsIgnoreCase(channel)) {
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
            Message.creator(
                new PhoneNumber(phoneNumber), // Needs E.164 format: e.g., +919876543210
                new PhoneNumber(smsSender),
                smsMessageBody
            ).create();
        }
    }

    // 2. Validate OTP with Expiration Checks (Universally handles both Email and Phone keys!)
    public boolean validateOtp(String target, String userInputOtp) {
        if (!otpCache.containsKey(target)) {
            return false; // No OTP requested for this identifier
        }

        OtpDetails details = otpCache.get(target);

        // Check if the 3-minute window has closed
        if (details.isExpired()) {
            otpCache.remove(target); // Clean up expired data
            return false;
        }

        // Match user code against cached code
        if (details.getCode().equals(userInputOtp)) {
            otpCache.remove(target); // Invalidate instantly on success to prevent replay attacks
            return true;
        }

        return false;
    }

	
}
