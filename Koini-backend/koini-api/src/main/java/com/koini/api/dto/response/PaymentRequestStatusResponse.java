package com.koini.api.dto.response;

public record PaymentRequestStatusResponse(
    String requestId,
    String status,
    String respondedAt
) {
}
