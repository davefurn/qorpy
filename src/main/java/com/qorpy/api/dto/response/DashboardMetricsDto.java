package com.qorpy.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardMetricsDto {
    private long totalInvoices;
    private double complianceRate;
    private long activeTaxpayers;
    private long openAlerts;
    private long totalInvoicesTrend;
    private double complianceRateTrend;
    private long activeTaxpayersTrend;
    private List<InvoiceTrendPointDto> invoiceTrend;
    private List<TopNonCompliantTaxpayerDto> topNonCompliant;
    private int periodDays;
}