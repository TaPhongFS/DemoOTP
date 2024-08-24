package com.example.demo.model;

import lombok.Data;

@Data
public class OtpRequest {
    private String email;
    private String otpCode;
}
