package com.koini.api.dto.response;

public record TransactionResponse(
    String transactionId,
    String type,
    String status,
    long amountKc,
    String amountUsd,
    long feeKc,
    String feeUsd,
    String reference,
    String createdAt
) {
}
