package com.qorpy.api.respository;

import com.qorpy.api.entity.Invoice;
import com.qorpy.api.enums.ComplianceFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    long countBySubmittedAtBetween(OffsetDateTime start, OffsetDateTime end);


    long countByComplianceFlagAndSubmittedAtBetween(ComplianceFlag flag, OffsetDateTime start, OffsetDateTime end);

    long countByTaxpayerId(UUID taxpayerId);

    long countByTaxpayerIdAndComplianceFlag(UUID taxpayerId, ComplianceFlag flag);

    @Query("SELECT i FROM Invoice i JOIN FETCH i.taxpayer WHERE i.taxpayer.id = :taxpayerId ORDER BY i.submittedAt DESC")
    List<Invoice> findTop10ByTaxpayerIdOrderBySubmittedAtDesc(@Param("taxpayerId") UUID taxpayerId,
                                                              org.springframework.data.domain.Pageable pageable);
}