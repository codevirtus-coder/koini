package com.koini.api.dto.response;

public record LoginResponse(
    String accessToken,
    String refreshToken,
    long accessTokenExpiresIn,
    UserSummaryResponse user
) {
}
