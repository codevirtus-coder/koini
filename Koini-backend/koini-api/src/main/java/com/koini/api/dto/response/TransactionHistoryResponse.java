package com.koini.api.dto.response;

import java.util.List;

public record TransactionHistoryResponse(
    WalletBalanceResponse wallet,
    List<TransactionResponse> transactions,
    int page,
    int size,
    long total
) {
}
