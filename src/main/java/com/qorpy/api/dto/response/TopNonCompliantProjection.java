package com.qorpy.api.dto.response;

import java.util.UUID;

/**
 * Type-safe projection for the US-019 top non-compliant taxpayers query.
 */
public interface TopNonCompliantProjection {
    UUID getTaxpayerId();
    String getTaxpayerName();
    String getTin();
    long getTotalInvoices();
    long getNonCompliantCount();
}
