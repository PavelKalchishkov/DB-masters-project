package org.example.model;

import java.math.BigDecimal;

public record Property(
        long propertyId,
        BigDecimal price,
        BigDecimal squareMeters,
        String latitude,
        String longitude,
        String city,
        String propertyType,  // 'garage' | 'house' | 'apartment'
        Long ownerId          // nullable
) {}
