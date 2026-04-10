package com.koini.api.controller.agent;

import com.koini.api.dto.request.ReverseWithdrawalRequest;
import com.koini.api.dto.request.TopUpRequest;
import com.koini.api.dto.request.TransactionHistoryFilter;
import com.koini.api.dto.request.WithdrawalRequest;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.DailySummaryResponse;
import com.koini.api.dto.response.FloatBalanceResponse;
import com.koini.api.dto.response.TopUpResponse;
import com.koini.api.dto.response.TransactionHistoryResponse;
import com.koini.api.dto.response.WithdrawalResponse;
import com.koini.api.service.agent.AgentService;
import com.koini.api.service.wallet.WalletService;
import com.koini.api.service.withdrawal.WithdrawalService;
import com.koini.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

  private final WalletService walletService;
  private final AgentService agentService;
  private final WithdrawalService withdrawalService;

  public AgentController(
      WalletService walletService,
      AgentService agentService,
      WithdrawalService withdrawalService
  ) {
    this.walletService = walletService;
    this.agentService = agentService;
    this.withdrawalService = withdrawalService;
  }

  @Operation(summary = "Top up", description = "Tops up passenger wallet")
  @PostMapping("/topup")
  public ResponseEntity<ApiResponse<TopUpResponse>> topup(
      @Valid @RequestBody TopUpRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    TopUpResponse response = walletService.topUp(request, SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Float balance", description = "Returns agent float balance")
  @GetMapping("/float/balance")
  public ResponseEntity<ApiResponse<FloatBalanceResponse>> floatBalance(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    FloatBalanceResponse response = agentService.getFloatBalance(SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Initiate withdrawal", description = "Initiates a withdrawal")
  @PostMapping("/withdrawal/initiate")
  public ResponseEntity<ApiResponse<WithdrawalResponse>> initiateWithdrawal(
      @Valid @RequestBody WithdrawalRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    WithdrawalResponse response = withdrawalService.initiateWithdrawal(request, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Confirm withdrawal", description = "Confirms withdrawal")
  @PostMapping("/withdrawal/{id}/confirm")
  public ResponseEntity<ApiResponse<Map<String, String>>> confirmWithdrawal(
      @PathVariable("id") String id,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    withdrawalService.confirmWithdrawal(id, SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "confirmed"), requestId));
  }

  @Operation(summary = "Reverse withdrawal", description = "Reverses a withdrawal")
  @PostMapping("/withdrawal/{id}/reverse")
  public ResponseEntity<ApiResponse<Map<String, String>>> reverseWithdrawal(
      @PathVariable("id") String id,
      @Valid @RequestBody ReverseWithdrawalRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    withdrawalService.reverseWithdrawal(id, SecurityUtils.currentUserId(), request, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "reversed"), requestId));
  }

  @Operation(summary = "Transactions", description = "Returns agent transactions")
  @GetMapping("/transactions")
  public ResponseEntity<ApiResponse<TransactionHistoryResponse>> transactions(
      @ModelAttribute TransactionHistoryFilter filter,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    TransactionHistoryResponse response = walletService.getTransactionHistory(SecurityUtils.currentUserId(), filter);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Daily summary", description = "Returns daily summary")
  @GetMapping("/daily-summary")
  public ResponseEntity<ApiResponse<DailySummaryResponse>> dailySummary(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    DailySummaryResponse response = agentService.getDailySummary(SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }
}
