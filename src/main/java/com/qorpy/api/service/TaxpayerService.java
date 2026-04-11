package com.qorpy.api.service;

import com.qorpy.api.dto.request.taxpayer.AnnotateTaxpayerRequest;
import com.qorpy.api.dto.request.taxpayer.SuspendTaxpayerRequest;
import com.qorpy.api.dto.response.TaxpayerAnnotationDto;
import com.qorpy.api.dto.response.TaxpayerDto;
import com.qorpy.api.entity.AdminUser;
import com.qorpy.api.entity.Taxpayer;
import com.qorpy.api.entity.TaxpayerAnnotation;
import com.qorpy.api.enums.AccountStatus;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.exception.BusinessException;
import com.qorpy.api.exception.ResourceNotFoundException;
import com.qorpy.api.respository.InvoiceRepository;
import com.qorpy.api.respository.TaxpayerAnnotationRepository;
import com.qorpy.api.respository.TaxpayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaxpayerService {

    private final TaxpayerRepository taxpayerRepository;
    private final TaxpayerAnnotationRepository annotationRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditLogService auditLogService;

    /**
     * US-009 — View Taxpayer List (paginated, searchable)
     */
    public Page<TaxpayerDto> listTaxpayers(String search, Pageable pageable) {
        Page<Taxpayer> page;
        if (search != null && !search.isBlank()) {
            page = taxpayerRepository.findByNameContainingIgnoreCaseOrTinContainingIgnoreCase(
                    search, search, pageable);
        } else {
            page = taxpayerRepository.findAll(pageable);
        }
        return page.map(t -> toListDto(t));
    }

    /**
     * US-010 — View Taxpayer Profile (with compliance summary)
     */
    public TaxpayerDto getTaxpayerProfile(UUID id) {
        Taxpayer taxpayer = findById(id);

        long total       = invoiceRepository.countByTaxpayerId(id);
        long compliant   = invoiceRepository.countByTaxpayerIdAndComplianceFlag(id, ComplianceFlag.COMPLIANT);
        long nonCompliant = invoiceRepository.countByTaxpayerIdAndComplianceFlag(id, ComplianceFlag.NON_COMPLIANT);
        double rate = total > 0 ? (compliant * 100.0 / total) : 0.0;

        boolean hasPendingFlag = annotationRepository
                .findByTaxpayerIdOrderByCreatedAtDesc(id)
                .stream()
                .anyMatch(TaxpayerAnnotation::isFlag);

        return TaxpayerDto.builder()
                .id(taxpayer.getId())
                .name(taxpayer.getName())
                .tin(taxpayer.getTin())
                .email(taxpayer.getEmail())
                .phone(taxpayer.getPhone())
                .kycStatus(taxpayer.getKycStatus())
                .subscriptionTier(taxpayer.getSubscriptionTier())
                .accountStatus(taxpayer.getAccountStatus())
                .suspensionReason(taxpayer.getSuspensionReason())
                .registeredAt(taxpayer.getRegisteredAt())
                .updatedAt(taxpayer.getUpdatedAt())
                .hasPendingFlag(hasPendingFlag)
                .totalInvoices(total)
                .compliantCount(compliant)
                .nonCompliantCount(nonCompliant)
                .complianceRate(Math.round(rate * 100.0) / 100.0)
                .build();
    }

    /**
     * US-011 — Get all annotations for a taxpayer
     */
    public List<TaxpayerAnnotationDto> getAnnotations(UUID taxpayerId) {
        findById(taxpayerId); // validate taxpayer exists
        return annotationRepository.findByTaxpayerIdOrderByCreatedAtDesc(taxpayerId)
                .stream()
                .map(this::toAnnotationDto)
                .collect(Collectors.toList());
    }

    /**
     * US-011 — Add flag or annotation to taxpayer
     */
    @Transactional
    public TaxpayerAnnotationDto annotate(UUID taxpayerId, AnnotateTaxpayerRequest request, AdminUser admin) {
        Taxpayer taxpayer = findById(taxpayerId);

        TaxpayerAnnotation annotation = TaxpayerAnnotation.builder()
                .taxpayer(taxpayer)
                .admin(admin)
                .note(request.getNote())
                .isFlag(request.isFlag())
                .build();

        TaxpayerAnnotation saved = annotationRepository.save(annotation);

        // Audit log differs depending on whether it's a flag or a note
        String actionType = request.isFlag() ? "TAXPAYER_FLAGGED" : "TAXPAYER_ANNOTATED";
        auditLogService.logActionWithDetails(admin, actionType, "TAXPAYER", taxpayerId,
                null, Map.of("note", request.getNote(), "isFlag", request.isFlag()));

        return toAnnotationDto(saved);
    }

    /**
     * US-012 — Suspend a taxpayer account (Super Admin only)
     */
    @Transactional
    public TaxpayerDto suspend(UUID taxpayerId, SuspendTaxpayerRequest request, AdminUser admin) {
        Taxpayer taxpayer = findById(taxpayerId);

        if (taxpayer.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new BusinessException("Taxpayer account is already suspended");
        }

        Map<String, Object> before = Map.of("accountStatus", taxpayer.getAccountStatus().name());

        taxpayer.setAccountStatus(AccountStatus.SUSPENDED);
        taxpayer.setSuspensionReason(request.getReason());
        taxpayer.setSuspendedBy(admin);
        Taxpayer saved = taxpayerRepository.save(taxpayer);

        auditLogService.logActionWithDetails(admin, "TAXPAYER_SUSPENDED", "TAXPAYER", taxpayerId,
                before,
                Map.of("accountStatus", AccountStatus.SUSPENDED.name(), "reason", request.getReason()));

        return toListDto(saved);
    }

    /**
     * US-012 — Reinstate a suspended taxpayer account (Super Admin only)
     */
    @Transactional
    public TaxpayerDto reinstate(UUID taxpayerId, AdminUser admin) {
        Taxpayer taxpayer = findById(taxpayerId);

        if (taxpayer.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new BusinessException("Taxpayer account is already active");
        }

        Map<String, Object> before = Map.of(
                "accountStatus", taxpayer.getAccountStatus().name(),
                "suspensionReason", taxpayer.getSuspensionReason() != null ? taxpayer.getSuspensionReason() : ""
        );

        taxpayer.setAccountStatus(AccountStatus.ACTIVE);
        taxpayer.setSuspensionReason(null);
        taxpayer.setSuspendedBy(null);
        Taxpayer saved = taxpayerRepository.save(taxpayer);

        auditLogService.logActionWithDetails(admin, "TAXPAYER_REINSTATED", "TAXPAYER", taxpayerId,
                before, Map.of("accountStatus", AccountStatus.ACTIVE.name()));

        return toListDto(saved);
    }

    // ---- helpers ----

    private Taxpayer findById(UUID id) {
        return taxpayerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Taxpayer not found with id: " + id));
    }

    /** Lightweight DTO for list view — no invoice counts fetched */
    private TaxpayerDto toListDto(Taxpayer t) {
        boolean hasPendingFlag = annotationRepository
                .findByTaxpayerIdOrderByCreatedAtDesc(t.getId())
                .stream()
                .anyMatch(TaxpayerAnnotation::isFlag);

        return TaxpayerDto.builder()
                .id(t.getId())
                .name(t.getName())
                .tin(t.getTin())
                .email(t.getEmail())
                .phone(t.getPhone())
                .kycStatus(t.getKycStatus())
                .subscriptionTier(t.getSubscriptionTier())
                .accountStatus(t.getAccountStatus())
                .suspensionReason(t.getSuspensionReason())
                .registeredAt(t.getRegisteredAt())
                .updatedAt(t.getUpdatedAt())
                .hasPendingFlag(hasPendingFlag)
                .build();
    }

    private TaxpayerAnnotationDto toAnnotationDto(TaxpayerAnnotation a) {
        return TaxpayerAnnotationDto.builder()
                .id(a.getId())
                .note(a.getNote())
                .isFlag(a.isFlag())
                .adminId(a.getAdmin().getId())
                .adminName(a.getAdmin().getFullName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}