package com.koini.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateRouteRequest(
    @Size(max = 200) String name,
    String origin,
    String destination,
    @Min(1) long fareKc,
    Boolean isActive
) {
}
