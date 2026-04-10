package com.qorpy.api.service;

import com.qorpy.api.dto.response.DashboardMetricsDto;
import com.qorpy.api.dto.response.InvoiceTrendPointDto;
import com.qorpy.api.dto.response.TopNonCompliantTaxpayerDto;
import com.qorpy.api.enums.AccountStatus;
import com.qorpy.api.enums.ComplianceFlag;
import com.qorpy.api.respository.InvoiceRepository;
import com.qorpy.api.respository.NotificationReadRepository;
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
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * EP-05 — Reporting & Analytics Dashboard (US-017, US-018, US-019)
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final TaxpayerRepository taxpayerRepository;
    private final NotificationReadRepository notificationReadRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * US-017 — Summary KPI metrics + US-018 trend chart + US-019 top non-compliant.
     * All metrics are scoped to the given period (7 / 30 / 90 days).
     */
    public DashboardMetricsDto getMetrics(int periodDays, UUID currentAdminId) {
        OffsetDateTime now       = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime periodStart  = now.minusDays(periodDays);
        OffsetDateTime prevStart    = periodStart.minusDays(periodDays);

        // ── Current period counts ──
        long totalInvoices   = invoiceRepository.countBySubmittedAtBetween(periodStart, now);
        long compliantCount  = invoiceRepository.countByComplianceFlagAndSubmittedAtBetween(
                ComplianceFlag.COMPLIANT, periodStart, now);
        long activeTaxpayers = taxpayerRepository.countByAccountStatus(AccountStatus.ACTIVE);
        long openAlerts          = notificationReadRepository.countByAdminIdAndIsReadFalse(currentAdminId); // unread = isRead false records for this admin

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

        List<Object[]> rows = invoiceRepository.findDailyComplianceCounts(from, to);

        // One entry per date: [compliant, nonCompliant, total]
        TreeMap<String, long[]> buckets = new TreeMap<>();

        for (Object[] row : rows) {
            String date   = row[0].toString();                          // LocalDate → String
            ComplianceFlag flag = (ComplianceFlag) row[1];
            long count    = ((Number) row[2]).longValue();

            buckets.computeIfAbsent(date, k -> new long[]{0L, 0L, 0L}); // [compliant, nonCompliant, total]
            if (flag == ComplianceFlag.COMPLIANT)     buckets.get(date)[0] += count;
            if (flag == ComplianceFlag.NON_COMPLIANT) buckets.get(date)[1] += count;
            buckets.get(date)[2] += count;
        }

        // For 90-day period: collapse daily buckets into weekly ones
        if (periodDays > 30) {
            return collapseToWeekly(buckets);
        }

        return buckets.entrySet().stream()
                .map(e -> toPoint(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    private List<InvoiceTrendPointDto> collapseToWeekly(TreeMap<String, long[]> dailyBuckets) {

        List<InvoiceTrendPointDto> weekly = new java.util.ArrayList<>();
        String weekStart = null;
        long[] acc = new long[3];
        int dayCount = 0;

        for (java.util.Map.Entry<String, long[]> e : dailyBuckets.entrySet()) {
            if (dayCount % 7 == 0) {
                if (weekStart != null) weekly.add(toPoint(weekStart, acc));
                weekStart = e.getKey();
                acc = new long[]{0L, 0L, 0L};
            }
            acc[0] += e.getValue()[0];
            acc[1] += e.getValue()[1];
            acc[2] += e.getValue()[2];
            dayCount++;
        }
        if (weekStart != null) weekly.add(toPoint(weekStart, acc)); // flush last bucket
        return weekly;
    }

    private InvoiceTrendPointDto toPoint(String date, long[] acc) {
        return InvoiceTrendPointDto.builder()
                .date(date)
                .compliant(acc[0])
                .nonCompliant(acc[1])
                .pending(acc[2] - acc[0] - acc[1])
                .total(acc[2])
                .build();
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