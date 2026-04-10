package com.koini.api.dto.response;

public record ReconciliationResponse(
    String date,
    long totalTopUpsKc,
    long totalFaresKc,
    long totalWithdrawalsKc,
    long totalTransfersKc,
    long netBalanceKc,
    long discrepancy,
    String status
) {
}
