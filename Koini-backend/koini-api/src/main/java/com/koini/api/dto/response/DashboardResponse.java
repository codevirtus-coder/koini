package com.koini.api.dto.response;

public record DashboardResponse(
    long passengers,
    long conductors,
    long agents,
    long totalWalletBalanceKc,
    String totalWalletBalanceUsd,
    long transactionsToday,
    long transactionVolumeKcToday,
    long activePaymentCodes,
    long flaggedAccounts
) {
}
