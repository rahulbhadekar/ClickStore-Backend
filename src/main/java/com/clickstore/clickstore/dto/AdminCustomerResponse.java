
package com.clickstore.clickstore.dto;

import java.time.OffsetDateTime;

public record AdminCustomerResponse(
        Long id,
        String fullName,
        String email,
        String accountStatus,
        OffsetDateTime createdAt,
        OffsetDateTime lastLoginAt
) {
}
