package com.qorpy.api.respository;

import com.qorpy.api.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.UUID;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    boolean existsByToken(String token);
    void deleteByExpiryDateBefore(Date now);
}