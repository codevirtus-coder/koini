package com.koini.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterAgentRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{10,15}$") String phone,
    @Size(max = 150) String fullName,
    @Size(max = 200) String businessName,
    @Size(max = 300) String location,
    @Min(0) long floatLimitKc
) {
}
