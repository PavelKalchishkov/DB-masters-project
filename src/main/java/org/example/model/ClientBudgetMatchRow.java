package org.example.model;

import java.math.BigDecimal;

public record ClientBudgetMatchRow(
        long clientId,
        String clientName,
        long propertyId,
        String city,
        String propertyType,
        BigDecimal price
) {}