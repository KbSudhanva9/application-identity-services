package com.auth_service.dto;

//public record ProfileResponse(
//		String userId,
//	    String name,
//	    String email,
//	    String role,
//	    String redirectUrl,
//	    String callbackUrl
//		){
//}

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileResponse {

    private String userId;

    private String name;

    private String email;

    private String role;
    
//    private String redirectUrl;
    
//    private String callbackUrl;
}