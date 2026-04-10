package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRouteRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank String origin,
    @NotBlank String destination,
    @Min(1) long fareKc
) {
}
