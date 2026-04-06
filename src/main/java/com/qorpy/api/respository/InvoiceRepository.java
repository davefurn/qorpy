package com.qorpy.api.respository;

import com.qorpy.api.entity.Invoice;
import com.qorpy.api.enums.ComplianceFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID>, JpaSpecificationExecutor<Invoice> {

    long countBySubmittedAtBetween(OffsetDateTime start, OffsetDateTime end);


    long countByComplianceFlagAndSubmittedAtBetween(ComplianceFlag flag, OffsetDateTime start, OffsetDateTime end);
}