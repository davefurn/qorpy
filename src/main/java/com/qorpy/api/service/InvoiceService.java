package com.qorpy.api.service;

import com.qorpy.api.dto.response.InvoiceDto;
import com.qorpy.api.dto.response.InvoiceHistoryDto;
import com.qorpy.api.entity.Invoice;
import com.qorpy.api.entity.InvoiceHistory;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.enums.InvoiceStatus;
import com.qorpy.api.exception.ResourceNotFoundException;
import com.qorpy.api.respository.InvoiceHistoryRepository;
import com.qorpy.api.respository.InvoiceRepository;
import com.qorpy.api.specification.InvoiceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final int EXPORT_MAX_ROWS = 5000;

    private final InvoiceRepository invoiceRepository;
    private final InvoiceHistoryRepository invoiceHistoryRepository;

    /**
     * US-013 / US-014 — Browse and filter invoice submissions.
     * All filter parameters are optional; null values are ignored.
     */
    public Page<InvoiceDto> listInvoices(
            UUID taxpayerId,
            String search,
            LocalDate from,
            LocalDate to,
            InvoiceStatus status,
            ComplianceFlag complianceFlag,
            Pageable pageable) {

        Specification<Invoice> spec = InvoiceSpecification.buildFilter(
                taxpayerId, search, from, to, status, complianceFlag);

        return invoiceRepository.findAll(spec, pageable)
                .map(this::toSummaryDto);
    }

    /**
     * US-015 — View full invoice detail including payload and NRS validation result.
     */
    public InvoiceDto getInvoiceDetail(UUID invoiceId) {
        Invoice invoice = findById(invoiceId);
        return toDetailDto(invoice);
    }

    /**
     * US-015 — View submission history (retries) for an invoice.
     */
    public List<InvoiceHistoryDto> getInvoiceHistory(UUID invoiceId) {
        findById(invoiceId); // validate invoice exists
        return invoiceHistoryRepository.findByInvoiceIdOrderByCreatedAtDesc(invoiceId)
                .stream()
                .map(this::toHistoryDto)
                .collect(Collectors.toList());
    }

    /**
     * US-016 — Export current filtered invoice list as CSV bytes.
     * Capped at EXPORT_MAX_ROWS (5,000) rows.
     *
     * @throws IllegalStateException if row count exceeds the limit (caller should warn client)
     */
    public byte[] exportCsv(
            UUID taxpayerId,
            String search,
            LocalDate from,
            LocalDate to,
            InvoiceStatus status,
            ComplianceFlag complianceFlag) {

        Specification<Invoice> spec = InvoiceSpecification.buildFilter(
                taxpayerId, search, from, to, status, complianceFlag);

        // Fetch up to EXPORT_MAX_ROWS + 1 to detect overflow
        Page<Invoice> page = invoiceRepository.findAll(spec,
                PageRequest.of(0, EXPORT_MAX_ROWS + 1));

        boolean truncated = page.getTotalElements() > EXPORT_MAX_ROWS;
        List<Invoice> rows = page.getContent().stream()
                .limit(EXPORT_MAX_ROWS)
                .collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();


        // Keep try-with-resources to automatically close the writer
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // BOM for Excel compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // Header
            writer.println("Invoice ID,Invoice Number,Taxpayer Name,Taxpayer TIN," +
                    "Submitted At,Amount,Status,Compliance Flag");

            // Rows
            for (Invoice inv : rows) {
                writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        inv.getId(),
                        inv.getInvoiceNumber(),
                        escapeCsv(inv.getTaxpayer().getName()),
                        escapeCsv(inv.getTaxpayer().getTin()),
                        inv.getSubmittedAt(),
                        inv.getAmount(),
                        inv.getSubmissionStatus(),
                        inv.getComplianceFlag() != null ? inv.getComplianceFlag() : "");
            }

            if (truncated) {
                writer.printf("%n# WARNING: Export limited to %d rows. Total matching: %d%n",
                        EXPORT_MAX_ROWS, page.getTotalElements());
            }
        }


        return baos.toByteArray();
    }

    // ---- private helpers ----

    private Invoice findById(UUID id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    /** Lightweight DTO for list/table view — no payload or NRS details */
    private InvoiceDto toSummaryDto(Invoice inv) {
        return InvoiceDto.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .taxpayerId(inv.getTaxpayer().getId())
                .taxpayerName(inv.getTaxpayer().getName())
                .taxpayerTin(inv.getTaxpayer().getTin())
                .amount(inv.getAmount())
                .submissionStatus(inv.getSubmissionStatus())
                .complianceFlag(inv.getComplianceFlag())
                .submittedAt(inv.getSubmittedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    /** Full DTO for detail view — includes payload and NRS validation result */
    private InvoiceDto toDetailDto(Invoice inv) {
        return InvoiceDto.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .taxpayerId(inv.getTaxpayer().getId())
                .taxpayerName(inv.getTaxpayer().getName())
                .taxpayerTin(inv.getTaxpayer().getTin())
                .amount(inv.getAmount())
                .submissionStatus(inv.getSubmissionStatus())
                .complianceFlag(inv.getComplianceFlag())
                .payload(inv.getPayload())
                .nrsValidationResult(inv.getNrsValidationResult())
                .submittedAt(inv.getSubmittedAt())
                .updatedAt(inv.getUpdatedAt())
                .build();
    }

    private InvoiceHistoryDto toHistoryDto(InvoiceHistory h) {
        return InvoiceHistoryDto.builder()
                .id(h.getId())
                .eventType(h.getEventType())
                .details(h.getDetails())
                .createdAt(h.getCreatedAt())
                .build();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }
}