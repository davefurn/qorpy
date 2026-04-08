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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic invoice filtering (US-014).
 *
 * Key constraint: root.fetch() must NOT be used inside count queries that Spring Data
 * runs for pagination. We detect count queries via query.getResultType() and skip
 * the fetch in that case, using a plain join instead.
 */
public class InvoiceSpecification {

    private InvoiceSpecification() {}

    /**
     * Combine all optional filters into one Specification.
     * Any null parameter is silently skipped.
     * Always returns a non-null Specification (falls back to match-all if no filters given).
     */
    public static Specification<Invoice> buildFilter(
            UUID taxpayerId,
            String search,
            LocalDate from,
            LocalDate to,
            InvoiceStatus status,
            ComplianceFlag complianceFlag) {

        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Determine if this is a count query (used by Spring Data for pagination).
            // fetch() is illegal in count queries — use a plain join instead.
            boolean isCountQuery = query.getResultType().equals(Long.class)
                    || query.getResultType().equals(long.class);

            if (search != null && !search.isBlank()) {
                // Join on taxpayer for name/TIN predicate
                Join<Invoice, Taxpayer> taxpayerJoin = root.join("taxpayer", JoinType.INNER);
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(taxpayerJoin.get("name")), pattern),
                        cb.like(cb.lower(taxpayerJoin.get("tin")),  pattern)
                ));
            } else {
                // No search predicate needed — but we still need taxpayer data in the
                // SELECT query to build the DTO. Use fetch for SELECT, plain join for COUNT.
                if (isCountQuery) {
                    root.join("taxpayer", JoinType.INNER);
                } else {
                    root.fetch("taxpayer", JoinType.INNER);
                }
            }

            // Filter by exact taxpayer UUID
            if (taxpayerId != null) {
                predicates.add(cb.equal(root.get("taxpayer").get("id"), taxpayerId));
            }

            // Filter by submission date >= fromDate (inclusive, start of day UTC)
            if (from != null) {
                OffsetDateTime start = from.atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(cb.greaterThanOrEqualTo(root.get("submittedAt"), start));
            }

            // Filter by submission date <= toDate (inclusive, end of day UTC)
            if (to != null) {
                OffsetDateTime end = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);
                predicates.add(cb.lessThan(root.get("submittedAt"), end));
            }

            // Filter by submission status
            if (status != null) {
                predicates.add(cb.equal(root.get("submissionStatus"), status));
            }

            if (complianceFlag != null) {
                predicates.add(cb.equal(root.get("complianceFlag"), complianceFlag));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };


    }
}