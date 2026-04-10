package com.koini.api.dto.response;

public record RedeemCodeResponse(
    String transactionId,
    String reference,
    long amountKc,
    String amountUsd,
    String passengerMaskedPhone,
    String routeName
) {
}
