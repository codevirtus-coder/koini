package com.koini.api.dto.response;

public record PesepayKeysStatusResponse(
    boolean configured,
    String integrationKeyMasked,
    String encryptionKeyMasked,
    String updatedAt
) {
}

