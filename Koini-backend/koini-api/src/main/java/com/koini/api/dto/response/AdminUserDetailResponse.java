package com.koini.api.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {
  private String userId;
  private String phone;
  private String fullName;
  private String role;
  private String status;
  private String kycLevel;
  private String createdAt;
  private String lastLogin;
  private WalletSummary wallet;
  private List<TransactionSummary> recentTransactions;
  private AgentDetail agentDetail;

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WalletSummary {
    private String walletId;
    private long balanceKc;
    private String balanceUsd;
    private String status;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class TransactionSummary {
    private String transactionId;
    private String type;
    private long amountKc;
    private String amountUsd;
    private String status;
    private String createdAt;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AgentDetail {
    private String agentId;
    private String businessName;
    private long floatBalanceKc;
    private String status;
  }
}
