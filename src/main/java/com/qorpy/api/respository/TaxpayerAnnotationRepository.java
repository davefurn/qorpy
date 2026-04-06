package com.qorpy.api.respository;

import com.qorpy.api.entity.TaxpayerAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaxpayerAnnotationRepository extends JpaRepository<TaxpayerAnnotation, UUID> {
    List<TaxpayerAnnotation> findByTaxpayerIdOrderByCreatedAtDesc(UUID taxpayerId);
}