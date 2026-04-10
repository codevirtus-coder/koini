package com.koini.api.dto.response;

public record RegisterResponse(
    String userId,
    String maskedPhone,
    String message
) {
}
