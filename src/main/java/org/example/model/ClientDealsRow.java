package org.example.model;

import java.math.BigDecimal;

public record ClientDealsRow(
        long clientId,
        String firstName,
        String lastName,
        long deals,
        BigDecimal totalSpent
) {}
