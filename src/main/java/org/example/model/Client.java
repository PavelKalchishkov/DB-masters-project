package org.example.model;

import java.math.BigDecimal;

public record Client(
        long personId,
        BigDecimal budget,
        String areaInterestedIn
) {}
