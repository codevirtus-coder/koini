package com.koini.api.dto.response;

public record DailySummaryResponse(
    long totalTopUpsKc,
    long totalWithdrawalsKc,
    String totalTopUpsUsd,
    String totalWithdrawalsUsd
) {
}
