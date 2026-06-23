package com.auth.beans;



import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.auth.dto.OtpDetails;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpServiceBean {

    private final JavaMailSender mailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.mail.from}")
    private String fromEmail;

    // Stores the email mapped against our new custom metadata object
    private final Map<String, OtpDetails> otpCache = new ConcurrentHashMap<>();

    public OtpServiceBean(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // 1. Generate 5-Digit OTP & Send Email
    public void sendOtpEmail(String toEmail) throws MessagingException {
        // Enforces exactly a 5-digit number (10000 - 99999)
        int code = 10000 + secureRandom.nextInt(90000);
        String otp = String.valueOf(code);
        
        // Cache the code with a strict 3-minute lifespan
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
        System.out.println("Generated OTP for " + toEmail + ": " + otp); // For debugging purposes only
        mailSender.send(mimeMessage);
    }

    // 2. Validate OTP with Expiration Checks
    public boolean validateOtp(String email, String userInputOtp) {
        if (!otpCache.containsKey(email)) {
            return false; // No OTP requested for this email
        }

        OtpDetails details = otpCache.get(email);

        // Check if the 3-minute window has closed
        if (details.isExpired()) {
            otpCache.remove(email); // Clean up expired data
            return false;
        }

        // Match user code against cached code
        if (details.getCode().equals(userInputOtp)) {
            otpCache.remove(email); // Invalidate instantly on success to prevent replay attacks
            return true;
        }

        return false;
    }
}
