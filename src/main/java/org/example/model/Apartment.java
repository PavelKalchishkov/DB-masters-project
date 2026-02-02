package org.example.model;

import java.math.BigDecimal;

public record Apartment(
        long propertyId,
        BigDecimal floor,
        BigDecimal numberOfBathrooms,
        BigDecimal numberOfRooms
) {}
