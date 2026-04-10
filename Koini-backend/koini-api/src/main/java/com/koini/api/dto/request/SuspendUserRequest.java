package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SuspendUserRequest(
    @NotBlank @Size(max = 255) String reason
) {
}
