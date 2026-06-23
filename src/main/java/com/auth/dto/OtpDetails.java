package com.auth.dto;


	
	import java.time.Instant;

	public class OtpDetails {
	    private final String code;
	    private final Instant expiryTime;

	    public OtpDetails(String code, long lifespanMinutes) {
	        this.code = code;
	        // Mark expiration exactly 3 minutes into the future
	        this.expiryTime = Instant.now().plusSeconds(lifespanMinutes * 60);
	    }

	    public String getCode() {
	        return code;
	    }

	    // Check if the current time has passed the allowed 3-minute mark
	    public boolean isExpired() {
	        return Instant.now().isAfter(expiryTime);
	    }
	}

