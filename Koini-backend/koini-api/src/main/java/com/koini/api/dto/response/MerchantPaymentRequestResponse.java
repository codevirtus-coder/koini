package com.koini.api.dto.response;

public record MerchantPaymentRequestResponse(
    String requestId,
    String passengerMaskedPhone,
    long amountKc,
    String amountUsd,
    String status,
    String createdAt,
    String expiresAt,
    String respondedAt,
    String routeId
) {
}

