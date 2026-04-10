package com.koini.api.dto.response;

public record TransferResponse(
    String transactionId,
    String reference,
    long amountKc,
    String amountUsd,
    String toMaskedPhone,
    long newBalanceKc
) {
}
