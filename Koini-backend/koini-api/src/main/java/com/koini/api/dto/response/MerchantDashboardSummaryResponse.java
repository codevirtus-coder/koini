package com.koini.api.dto.response;

import java.util.List;

public record MerchantDashboardSummaryResponse(
    Wallet wallet,
    Today today,
    Requests requests,
    List<TransactionResponse> recentTransactions
) {

  public record Wallet(
      long balanceKc,
      String balanceUsd,
      long points,
      String status
  ) {
  }

  public record Today(
      long faresCount,
      long earningsKc,
      String earningsUsd,
      String firstFareAt,
      String lastFareAt
  ) {
  }

  public record Requests(
      long pendingCount,
      String lastRequestAt
  ) {
  }
}

