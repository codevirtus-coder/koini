package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;

public record InitiatePesepayTopupRequest(
    @Min(1) long amountKc,
    String currencyCode
) {
}

