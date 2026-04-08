package com.qorpy.api.controller;

import com.qorpy.api.dto.response.InvoiceDto;
import com.qorpy.api.dto.response.InvoiceHistoryDto;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.enums.InvoiceStatus;
import com.qorpy.api.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * US-013 / US-014 — Browse all invoice submissions with optional filters.
     * Default sort: submittedAt DESC, 25 per page.
    // *
     * Query params (all optional):
     *   taxpayerId   — filter by exact taxpayer UUID
     *   search       — partial match on taxpayer name or TIN
     *   from         — submission date >= (yyyy-MM-dd)
     *   to           — submission date <= (yyyy-MM-dd)
     *   status       — PENDING | ACCEPTED | REJECTED
     *   complianceFlag — COMPLIANT | NON_COMPLIANT
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<Page<InvoiceDto>> listInvoices(
            @RequestParam(required = false) UUID taxpayerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) ComplianceFlag complianceFlag,
            @PageableDefault(size = 25, sort = "submittedAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                invoiceService.listInvoices(taxpayerId, search, from, to, status, complianceFlag, pageable));
    }

    /**
     * US-015 — View full invoice detail (payload + NRS validation result).
     */
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<InvoiceDto> getInvoiceDetail(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoiceDetail(invoiceId));
    }

    /**
     * US-015 — View submission history / retries for an invoice.
     */
    @GetMapping("/{invoiceId}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER', 'VIEWER')")
    public ResponseEntity<List<InvoiceHistoryDto>> getInvoiceHistory(@PathVariable UUID invoiceId) {
        return ResponseEntity.ok(invoiceService.getInvoiceHistory(invoiceId));
    }

    /**
     * US-016 — Export filtered invoice list as CSV (max 5,000 rows).
     * Applies the same filter params as the list endpoint.
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COMPLIANCE_OFFICER')")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) UUID taxpayerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) InvoiceStatus status,
            @RequestParam(required = false) ComplianceFlag complianceFlag) {

        byte[] csv = invoiceService.exportCsv(taxpayerId, search, from, to, status, complianceFlag);

        String filename = "invoices-export-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csv);
    }
}