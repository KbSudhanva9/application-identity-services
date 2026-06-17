package com.auth_service.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserFilterRequest {

    private String name;

    private String email;

    private String role;

//    private Double usedDeposit;
//
//    private Double deposit;

    private Boolean isActive;

}