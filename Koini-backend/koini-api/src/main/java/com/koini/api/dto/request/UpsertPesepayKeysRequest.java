package com.koini.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpsertPesepayKeysRequest(
    @NotBlank String integrationKey,
    @NotBlank String encryptionKey
) {
}

