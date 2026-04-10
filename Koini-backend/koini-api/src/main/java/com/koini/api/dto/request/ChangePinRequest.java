package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePinRequest(
    @NotBlank String currentPin,
    @NotBlank @Pattern(regexp = "^[0-9]{4}$") String newPin,
    @NotBlank @Pattern(regexp = "^[0-9]{4}$") String confirmNewPin
) {
}
