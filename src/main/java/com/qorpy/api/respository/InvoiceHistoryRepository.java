package com.qorpy.api.respository;

import com.qorpy.api.entity.InvoiceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceHistoryRepository extends JpaRepository<InvoiceHistory, UUID> {
    List<InvoiceHistory> findByInvoiceIdOrderByCreatedAtDesc(UUID invoiceId);
}