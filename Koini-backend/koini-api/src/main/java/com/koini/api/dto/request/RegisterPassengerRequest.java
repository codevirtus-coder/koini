package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPassengerRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{10,15}$") String phone,
    @NotBlank @Size(min = 8, max = 72) String password,
    @Size(max = 150) String fullName
) {
}
