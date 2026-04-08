package com.qorpy.api.specification;

import com.qorpy.api.entity.Invoice;
import com.qorpy.api.entity.Taxpayer;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.enums.InvoiceStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * JPA Specifications for dynamic invoice filtering (US-014).
 * Each method returns a composable Specification that can be chained with .and()
 */
public class InvoiceSpecification {

    private InvoiceSpecification() {}

    /** Filter by a specific taxpayer UUID */
    public static Specification<Invoice> hasTaxpayerId(UUID taxpayerId) {
        if (taxpayerId == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("taxpayer").get("id"), taxpayerId);
    }

    /** Filter by taxpayer name OR tin (case-insensitive partial match) */
    public static Specification<Invoice> taxpayerMatches(String search) {
        if (search == null || search.isBlank()) return null;
        return (root, query, cb) -> {
            Join<Invoice, Taxpayer> taxpayerJoin = root.join("taxpayer", JoinType.INNER);
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(taxpayerJoin.get("name")), pattern),
                    cb.like(cb.lower(taxpayerJoin.get("tin")),  pattern)
            );
        };
    }

    /** Filter by submission date >= fromDate (inclusive, start of day UTC) */
    public static Specification<Invoice> submittedFrom(LocalDate fromDate) {
        if (fromDate == null) return null;
        return (root, query, cb) -> {
            OffsetDateTime start = fromDate.atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.greaterThanOrEqualTo(root.get("submittedAt"), start);
        };
    }

    /** Filter by submission date <= toDate (inclusive, end of day UTC) */
    public static Specification<Invoice> submittedTo(LocalDate toDate) {
        if (toDate == null) return null;
        return (root, query, cb) -> {
            OffsetDateTime end = toDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
            return cb.lessThan(root.get("submittedAt"), end);
        };
    }

    /** Filter by invoice submission status */
    public static Specification<Invoice> hasStatus(InvoiceStatus status) {
        if (status == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("submissionStatus"), status);
    }

    /** Filter by NRS compliance flag */
    public static Specification<Invoice> hasComplianceFlag(ComplianceFlag flag) {
        if (flag == null) return null;
        return (root, query, cb) ->
                cb.equal(root.get("complianceFlag"), flag);
    }

    /**
     * Combine all optional filters into one Specification.
     * Any null filter is silently ignored by Spring Data.
     */
    public static Specification<Invoice> buildFilter(
            UUID taxpayerId,
            String search,
            LocalDate from,
            LocalDate to,
            InvoiceStatus status,
            ComplianceFlag complianceFlag) {

        return Specification.where(hasTaxpayerId(taxpayerId))
                .and(taxpayerMatches(search))
                .and(submittedFrom(from))
                .and(submittedTo(to))
                .and(hasStatus(status))
                .and(hasComplianceFlag(complianceFlag));
    }
}