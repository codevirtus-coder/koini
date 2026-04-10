package com.koini.api.dto.response;

public record ShiftReportResponse(
    long totalEarningsKc,
    String totalEarningsUsd
) {
}
