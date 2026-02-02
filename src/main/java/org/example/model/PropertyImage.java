package org.example.model;

public record PropertyImage(
        long imageId,
        long listingId,
        byte[] imageData,   // BYTEA
        String imageUrl
) {}