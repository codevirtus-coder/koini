package com.koini.api.dto.response;

public record CancelPaymentRequestResponse(
    String requestId,
    String status
) {
}

