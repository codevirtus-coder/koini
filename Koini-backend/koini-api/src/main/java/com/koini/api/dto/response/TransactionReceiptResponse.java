package com.koini.api.dto.response;

public record TransactionReceiptResponse(
    String transactionId,
    String reference,
    String type,
    String status,
    long amountKc,
    String amountUsd,
    long feeKc,
    String feeUsd,
    String description,
    String createdAt
) {
}

