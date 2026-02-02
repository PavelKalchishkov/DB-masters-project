package org.example.model;

import java.math.BigDecimal;

public record House(
        long propertyId,
        BigDecimal numberOfFloors,
        BigDecimal gardenSizeM2,
        BigDecimal numberOfBathrooms,
        BigDecimal numberOfRooms
) {}