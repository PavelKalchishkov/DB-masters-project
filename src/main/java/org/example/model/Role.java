package org.example.model;

public record Role(
        long roleId,
        String roleType,
        boolean hasFullAccess,
        boolean canPost,
        boolean canAuthorizeSale
) {}