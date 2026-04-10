package com.koini.api.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 150) String fullName
) {
}
