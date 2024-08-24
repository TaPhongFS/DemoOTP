package com.example.demo.jpa;

import com.example.demo.entity.OtpEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtpRepositoryJpa extends JpaRepository<OtpEntity, Long> {
    @Modifying
    @Query(value = " update otp set is_active = 'N' where lower(email) = ?1 and is_active = 'Y' ", nativeQuery = true)
    void deActiveOtp(String email);

    List<OtpEntity> getOtpEntitiesByEmailEqualsAndIsActive(String email, String isActive);
}
