package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RedeemCodeRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{6}$") String code
) {
}
