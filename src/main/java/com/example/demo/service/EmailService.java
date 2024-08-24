package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmailService {
    void sendMail(String to, String subject, String text, List<MultipartFile> attachments) throws Exception;
}
