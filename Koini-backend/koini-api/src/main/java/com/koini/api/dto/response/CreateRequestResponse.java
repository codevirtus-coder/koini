package com.koini.api.dto.response;

public record CreateRequestResponse(
    String requestId,
    String passengerMaskedPhone,
    long amountKc,
    String expiresAt
) {
}
