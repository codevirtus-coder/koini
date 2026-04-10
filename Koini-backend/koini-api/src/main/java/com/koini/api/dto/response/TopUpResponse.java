package com.koini.api.dto.response;

public record TopUpResponse(
    String transactionId,
    String reference,
    long amountKc,
    String amountUsd,
    long newBalanceKc,
    String passengerMaskedPhone
) {
}
