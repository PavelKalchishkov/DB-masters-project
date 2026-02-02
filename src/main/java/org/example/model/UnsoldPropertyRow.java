package org.example.model;

import java.math.BigDecimal;

public record UnsoldPropertyRow(
        long propertyId,
        String propertyType,
        String city,
        BigDecimal price
) {}
