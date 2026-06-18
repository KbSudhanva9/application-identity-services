package com.auth_service.dto;

public record ApiResponse<T>(
	    String message,
		T data
		) {
}

//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class ApiResponse<T> {
//
////    private boolean success;
//
//    private String message;
//
//    private T data;
//}
