package org.example.model;

import java.math.BigDecimal;

public record SuccessfulDeal(
        long dealId,
        long propertyId,
        BigDecimal finalPrice,
        long agentId,
        long clientId
) {}
