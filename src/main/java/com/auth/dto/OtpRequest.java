package com.auth.dto;

public record OtpRequest(
        String email,       // Stores the email address (for email)
        String channel,     // "email", "sms", or "whatsapp"
        String phone, //  mobile number (for sms/whatapp)
        String otp,          // The validation code (null when requesting an OTP)
        String newPassword
) {
    
   
    public static OtpRequest forEmail(String email, String otp) {
        return new OtpRequest(email, "email", null, otp, null);
    }

   
    public static OtpRequest forMobile(String channel, String phoneNumber, String otp) {
        if (channel == null || (!channel.equalsIgnoreCase("sms") && !channel.equalsIgnoreCase("whatsapp"))) {
            throw new IllegalArgumentException("Channel must be 'sms' or 'whatsapp' for mobile requests.");
        }
        return new OtpRequest(null, channel.toLowerCase(), phoneNumber, otp, null);
    }
}

