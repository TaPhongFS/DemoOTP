package com.example.demo.service;

import com.example.demo.model.OtpRequest;
import com.example.demo.utils.ResponseData;
import org.springframework.http.ResponseEntity;

public interface OtpService {
    ResponseEntity<ResponseData<Object>> generateOtp(String email);

    ResponseEntity<ResponseData<Object>> verifyOtp(OtpRequest request);
}
