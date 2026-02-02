package org.example.model;

public record PropertyOwnerRow(
        long propertyId,
        String propertyType,
        String city,
        Long ownerId,
        String ownerName
) {}