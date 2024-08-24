package com.example.demo.service.impl;

import com.example.demo.entity.OtpEntity;
import com.example.demo.jpa.OtpRepositoryJpa;
import com.example.demo.model.OtpRequest;
import com.example.demo.service.EmailService;
import com.example.demo.service.OtpService;
import com.example.demo.utils.ResponseData;
import com.example.demo.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpRepositoryJpa otpRepositoryJpa;
    private final EmailService emailService;
    @Override
    @Transactional
    public ResponseEntity<ResponseData<Object>> generateOtp(String email) {
        email = StringUtils.trim(email);
        email = StringUtils.lowerCase(email);
        if (!EmailValidator.getInstance().isValid(email)) {
            return ResponseUtils.error("Email không đúng định dạng");
        }
        otpRepositoryJpa.deActiveOtp(email);
        String secretKey = generateBase32SecretKey();
        String otpCode = generateTOTP(secretKey, 6, 30);
        OtpEntity entity = OtpEntity.builder().email(email).createAt(LocalDateTime.now())
                    .otpCode(otpCode).duration(90L).isActive("Y").build();

        try {
            emailService.sendMail(email, "Generate Otp", otpCode, null);
        } catch (Exception e) {
            return ResponseUtils.error("Lỗi khi gửi email otp!");
        }
        otpRepositoryJpa.save(entity);
        return ResponseUtils.success("OTP generated and sent successfully.");
    }

    @Override
    @Transactional
    public ResponseEntity<ResponseData<Object>> verifyOtp(OtpRequest request) {
        List<OtpEntity> entityList = otpRepositoryJpa.getOtpEntitiesByEmailEqualsAndIsActive(request.getEmail(), "Y");
        if (entityList.isEmpty()) {
            return ResponseUtils.error("Không tồn tại Otp theo email: " + request.getEmail());
        }

        for (OtpEntity item : entityList) {
            if (!StringUtils.equalsIgnoreCase(item.getOtpCode(), request.getOtpCode())) {
                return ResponseUtils.error("Otp không hợp lệ!");
            }

            if (item.getCreateAt().plusSeconds(item.getDuration()).isBefore(LocalDateTime.now())) {
                return ResponseUtils.error("Otp đã hết hạn!");
            }
        }

        return ResponseUtils.success();
    }
    public String generateTOTP(String secretKey, int digits, int timeStepSeconds) {
        long currentTime = Instant.now().getEpochSecond();
        long timeCounter = currentTime / timeStepSeconds;

        return generateHOTP(secretKey, timeCounter, digits);
    }

    public String generateHOTP(String secretKey, long counter, int digits) {
        byte[] key = new Base32().decode(secretKey);
        byte[] counterBytes = ByteBuffer.allocate(8).putLong(counter).array();

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA1");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(counterBytes);

            int offset = hash[hash.length - 1] & 0x0F;
            int binary =
                    ((hash[offset] & 0x7F) << 24) |
                            ((hash[offset + 1] & 0xFF) << 16) |
                            ((hash[offset + 2] & 0xFF) << 8) |
                            (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating OTP", e);
        }
    }

    public static String generateBase32SecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);


        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

}
