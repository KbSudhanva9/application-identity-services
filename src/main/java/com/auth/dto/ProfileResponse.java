package com.auth.dto;

//public record ProfileResponse(
//		String userId,
//	    String name,
//	    String email,
//	    String role,
//	    String redirectUrl,
//	    String callbackUrl
//		){
//}


public record ProfileResponse (

      String userId,

      String name,

      String email,

      String role) {
    
//    private String redirectUrl;
    
//    private String callbackUrl;
}