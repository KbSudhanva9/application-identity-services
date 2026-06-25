package com.auth.dto;





public record ForceResetPasswordRequest (

     String userId,

     String password) {
}