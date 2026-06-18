package com.auth_service.dto;

//import jakarta.validation.constraints.NotNull;
//
//public record UpdateUserStatus(
//		@NotNull
//		String userId,
//		
//		@NotNull
//		Boolean isActive) {}

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserStatus {

    private String userId;

    private Boolean isActive;
}