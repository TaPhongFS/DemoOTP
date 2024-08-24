package com.example.demo;

import com.example.demo.model.OtpRequest;
import com.example.demo.service.OtpService;
import com.example.demo.utils.ResponseData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Scanner;

@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);
    private final OtpService otpService;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            boolean otpGeneratedSuccessfully = false;
            String email = "";

            while (!otpGeneratedSuccessfully) {
                System.out.println("Enter your email to generate an OTP:");
                email = scanner.nextLine();

                // Generate OTP
                ResponseData<Object> generateResponse = otpService.generateOtp(email).getBody();
                System.out.println(generateResponse.getMessage());
                if (generateResponse.getMessage().equals("Successful!")) {
                    otpGeneratedSuccessfully = true;
                }
            }
            System.out.println("Enter the OTP you received:");
            String otpCode = scanner.nextLine();
            // Create a mock OtpRequest
            OtpRequest otpRequest = new OtpRequest();
            otpRequest.setEmail(email);
            otpRequest.setOtpCode(otpCode);

            // Verify OTP
            ResponseData<Object> verifyResponse = otpService.verifyOtp(otpRequest).getBody();
            System.out.println(verifyResponse.getMessage());
            System.exit(0);
        };
    }

}
