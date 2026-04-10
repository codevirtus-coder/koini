package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TransferRequest(
    @NotBlank String toPhone,
    @Min(1) long amountKc,
    @NotBlank @Pattern(regexp = "^[0-9]{4}$") String pin
) {
}
