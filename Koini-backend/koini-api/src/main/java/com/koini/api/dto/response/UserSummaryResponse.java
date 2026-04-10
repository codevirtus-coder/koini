package com.koini.api.dto.response;

public record UserSummaryResponse(
    String userId,
    String maskedPhone,
    String fullName,
    String role,
    String status
) {
}
