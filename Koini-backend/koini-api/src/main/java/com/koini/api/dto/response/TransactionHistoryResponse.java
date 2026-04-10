package com.koini.api.dto.response;

import java.util.List;

public record TransactionHistoryResponse(
    List<TransactionResponse> transactions,
    int page,
    int size,
    long total
) {
}
