package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record WithdrawalRequest(
    @NotBlank String passengerPhone,
    @Min(1) long amountKc
) {
}
