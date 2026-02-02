package org.example.model;

public record Listing(
        long listingId,
        String typeOfListing,
        String description,
        String notes
) {}
