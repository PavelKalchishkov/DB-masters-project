package org.example.model;

import java.math.BigDecimal;

public record AgentSalesRow(
        long agentId,
        String firstName,
        String lastName,
        long deals,
        BigDecimal totalSales
) {}