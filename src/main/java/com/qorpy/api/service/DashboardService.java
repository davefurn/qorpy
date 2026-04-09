package com.qorpy.api.service;

import com.qorpy.api.dto.response.DashboardMetricsDto;
import com.qorpy.api.dto.response.InvoiceTrendPointDto;
import com.qorpy.api.dto.response.TopNonCompliantTaxpayerDto;
import com.qorpy.api.enums.AccountStatus;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.respository.InvoiceRepository;
import com.qorpy.api.respository.NotificationRepository;
import com.qorpy.api.respository.TaxpayerRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * EP-05 — Reporting & Analytics Dashboard (US-017, US-018, US-019)
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final TaxpayerRepository taxpayerRepository;
    private final NotificationRepository notificationRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * US-017 — Summary KPI metrics + US-018 trend chart + US-019 top non-compliant.
     * All metrics are scoped to the given period (7 / 30 / 90 days).
     */
    public DashboardMetricsDto getMetrics(int periodDays) {
        OffsetDateTime now       = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime periodStart  = now.minusDays(periodDays);
        OffsetDateTime prevStart    = periodStart.minusDays(periodDays);

        // ── Current period counts ──
        long totalInvoices   = invoiceRepository.countBySubmittedAtBetween(periodStart, now);
        long compliantCount  = invoiceRepository.countByComplianceFlagAndSubmittedAtBetween(
                ComplianceFlag.COMPLIANT, periodStart, now);
        long activeTaxpayers = taxpayerRepository.countByAccountStatus(AccountStatus.ACTIVE);
        long openAlerts      = notificationRepository.count(); // all notifications as open alerts

        double complianceRate = totalInvoices > 0
                ? Math.round((compliantCount * 100.0 / totalInvoices) * 100.0) / 100.0
                : 0.0;

        // ── Previous period counts (for trend indicators) ──
        long prevTotal      = invoiceRepository.countBySubmittedAtBetween(prevStart, periodStart);
        long prevCompliant  = invoiceRepository.countByComplianceFlagAndSubmittedAtBetween(
                ComplianceFlag.COMPLIANT, prevStart, periodStart);
        double prevRate     = prevTotal > 0
                ? Math.round((prevCompliant * 100.0 / prevTotal) * 100.0) / 100.0
                : 0.0;

        // ── US-018 Invoice Trend Chart ──
        List<InvoiceTrendPointDto> trend = buildTrendChart(periodDays, periodStart, now);

        // ── US-019 Top Non-Compliant Taxpayers ──
        List<TopNonCompliantTaxpayerDto> topNonCompliant = getTopNonCompliant(periodStart, now);

        return DashboardMetricsDto.builder()
                .totalInvoices(totalInvoices)
                .complianceRate(complianceRate)
                .activeTaxpayers(activeTaxpayers)
                .openAlerts(openAlerts)
                .totalInvoicesTrend(totalInvoices - prevTotal)
                .complianceRateTrend(Math.round((complianceRate - prevRate) * 100.0) / 100.0)
                .activeTaxpayersTrend(0L) // taxpayer count doesn't have a prior period concept
                .invoiceTrend(trend)
                .topNonCompliant(topNonCompliant)
                .periodDays(periodDays)
                .build();
    }

    /**
     * US-018 — Build daily data points for the trend chart.
     * For periods <= 30 days: daily buckets. For 90 days: weekly buckets.
     */
    private List<InvoiceTrendPointDto> buildTrendChart(
            int periodDays, OffsetDateTime from, OffsetDateTime to) {

        List<InvoiceTrendPointDto> points = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        boolean weekly = periodDays > 30;
        int stepDays   = weekly ? 7 : 1;

        OffsetDateTime cursor = from;
        while (cursor.isBefore(to)) {
            OffsetDateTime next = cursor.plusDays(stepDays).isAfter(to)
                    ? to : cursor.plusDays(stepDays);

            long compliant    = invoiceRepository.countByComplianceFlagAndSubmittedAtBetween(
                    ComplianceFlag.COMPLIANT, cursor, next);
            long nonCompliant = invoiceRepository.countByComplianceFlagAndSubmittedAtBetween(
                    ComplianceFlag.NON_COMPLIANT, cursor, next);
            long total        = invoiceRepository.countBySubmittedAtBetween(cursor, next);
            long pending      = total - compliant - nonCompliant;

            points.add(InvoiceTrendPointDto.builder()
                    .date(cursor.toLocalDate().format(fmt))
                    .compliant(compliant)
                    .nonCompliant(nonCompliant)
                    .pending(pending)
                    .total(total)
                    .build());

            cursor = next;
        }
        return points;
    }

    /**
     * US-019 — Top 10 taxpayers by non-compliant invoice count in the period.
     */
    @SuppressWarnings("unchecked")
    private List<TopNonCompliantTaxpayerDto> getTopNonCompliant(
            OffsetDateTime from, OffsetDateTime to) {

        List<Object[]> rows = em.createQuery(
                        "SELECT t.id, t.name, t.tin, " +
                                "COUNT(i.id), " +
                                "SUM(CASE WHEN i.complianceFlag = 'NON_COMPLIANT' THEN 1 ELSE 0 END) " +
                                "FROM Invoice i JOIN i.taxpayer t " +
                                "WHERE i.submittedAt BETWEEN :from AND :to " +
                                "GROUP BY t.id, t.name, t.tin " +
                                "ORDER BY SUM(CASE WHEN i.complianceFlag = 'NON_COMPLIANT' THEN 1 ELSE 0 END) DESC"
                )
                .setParameter("from", from)
                .setParameter("to", to)
                .setMaxResults(10)
                .getResultList();

        List<TopNonCompliantTaxpayerDto> result = new ArrayList<>();
        for (Object[] row : rows) {
            long total        = ((Number) row[3]).longValue();
            long nonCompliant = ((Number) row[4]).longValue();
            double rate       = total > 0
                    ? Math.round(((total - nonCompliant) * 100.0 / total) * 100.0) / 100.0
                    : 0.0;

            result.add(TopNonCompliantTaxpayerDto.builder()
                    .taxpayerId((java.util.UUID) row[0])
                    .taxpayerName((String) row[1])
                    .tin((String) row[2])
                    .totalInvoices(total)
                    .nonCompliantCount(nonCompliant)
                    .complianceRate(rate)
                    .build());
        }
        return result;
    }
}