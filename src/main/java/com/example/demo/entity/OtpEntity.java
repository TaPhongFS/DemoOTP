package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "otp")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "otp_id")
    private Long otpId;

    @Column(name = "email")
    private String email;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "is_active")
    private String isActive;

    private Long duration;

    private LocalDateTime createAt;
}
