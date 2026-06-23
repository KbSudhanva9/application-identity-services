package com.auth.dto;

//import jakarta.validation.constraints.NotNull;
//
//public record UpdateUserStatus(
//		@NotNull
//		String userId,
//		
//		@NotNull
//		Boolean isActive) {}


public record UpdateUserStatus (

     String userId,

    boolean isActive) {
}