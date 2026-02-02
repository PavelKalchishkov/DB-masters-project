package org.example.model;

public record Preference(
        long preferenceId,
        long clientId,
        String preferenceType
) {}