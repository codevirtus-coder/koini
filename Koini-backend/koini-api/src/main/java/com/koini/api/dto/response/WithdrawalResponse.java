package com.koini.api.dto.response;

public record WithdrawalResponse(
    String withdrawalId,
    long amountKc,
    String amountUsd,
    String status,
    String expiresAt
) {
}
