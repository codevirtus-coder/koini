package com.koini.api.dto.response;

public record WalletBalanceResponse(
    String walletId,
    long balanceKc,
    long points,
    String balanceUsd,
    String status,
    String lastUpdated
) {
}
