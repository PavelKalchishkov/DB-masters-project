package org.example.model;

public record Person(
        long personId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber
) {}
