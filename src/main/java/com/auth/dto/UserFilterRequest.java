package com.auth.dto;


public record UserFilterRequest( 
      String name,
      String email,
      String role,
      Boolean isActive,
      String phone) {

}