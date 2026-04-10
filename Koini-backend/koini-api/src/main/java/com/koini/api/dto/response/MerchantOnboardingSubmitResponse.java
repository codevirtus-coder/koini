package com.koini.api.dto.response;

public record MerchantOnboardingSubmitResponse(
    boolean submitted,
    String status
) {
}

