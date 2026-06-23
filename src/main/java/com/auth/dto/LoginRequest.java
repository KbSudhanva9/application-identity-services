package com.auth.dto;

import java.util.Objects;

public record LoginRequest(String email, String password) {

    // 1. JSON Styled toString (Masks the password for security)
    @Override
    public String toString() {
        return """
               {
                 "email": "%s",
                 "password": "********"
               }
               """.formatted(email);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof LoginRequest other)) return false;
        
        if (this.email == null || other.email == null) {
            return this.email == other.email;
        }
        return this.email.equalsIgnoreCase(other.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email != null ? email.toLowerCase() : null);
    }
}
