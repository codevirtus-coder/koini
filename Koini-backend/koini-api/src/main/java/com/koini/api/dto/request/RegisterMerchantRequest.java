package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterMerchantRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{10,15}$") String phone,
    @Size(max = 150) String fullName,
    String routeId
) {
}

