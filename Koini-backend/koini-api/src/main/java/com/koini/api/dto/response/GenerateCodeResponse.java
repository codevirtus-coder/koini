package com.koini.api.dto.response;

public record GenerateCodeResponse(
    String code,
    String expiresAt,
    long amountKc,
    String amountUsd,
    long feeKc,
    String feeUsd,
    long totalDeductionKc
) {
}
