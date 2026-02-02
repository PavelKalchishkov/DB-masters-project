package org.example.model;

import java.math.BigDecimal;

public record AvgSaleByTypeRow(
        String propertyType,
        long deals,
        BigDecimal avgFinalPrice
) {}
