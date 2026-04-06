package com.qorpy.api.respository;

import com.qorpy.api.entity.Taxpayer;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

public interface TaxpayerRepository extends JpaRepository<Taxpayer, UUID> {
    Optional<Taxpayer> findByTin(String tin);

    Page<Taxpayer> findByNameContainingIgnoreCaseOrTinContainingIgnoreCase(
            String name, String tin, Pageable pageable);
}