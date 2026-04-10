package com.koini.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record TopUpRequest(
    @NotBlank String holderPhone,
    @Min(100) @Max(500000) long amountKc
) {
}
