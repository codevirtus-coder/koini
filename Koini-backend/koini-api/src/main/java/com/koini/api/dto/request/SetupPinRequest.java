package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetupPinRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{4}$") String pin,
    @NotBlank @Pattern(regexp = "^[0-9]{4}$") String confirmPin
) {
}
