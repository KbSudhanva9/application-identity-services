package com.auth_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "otp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OTP {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "user_id")
    private String user_id;

    @Column(name = "otp")
    private String otp;

    @Column(name = "otp_type")
    private String otpType;

    @Column(name = "is_verified")
    private boolean isVerified;

    @Column(name = "sent_on")
    private LocalDateTime sentOn;

    @Column(name = "expires_on")
    private LocalDateTime expiresOn;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_active")
    private boolean isActive;
    
}