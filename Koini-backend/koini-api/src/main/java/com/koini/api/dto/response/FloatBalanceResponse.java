package com.koini.api.dto.response;

public record FloatBalanceResponse(
    long floatBalanceKc,
    String floatBalanceUsd
) {
}
