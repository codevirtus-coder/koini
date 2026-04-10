package com.koini.api.controller.merchant;

import com.koini.api.dto.request.CreateRequestRequest;
import com.koini.api.dto.request.RedeemCodeRequest;
import com.koini.api.dto.request.TransactionHistoryFilter;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.CreateRequestResponse;
import com.koini.api.dto.response.PaymentRequestStatusResponse;
import com.koini.api.dto.response.RedeemCodeResponse;
import com.koini.api.dto.response.RouteResponse;
import com.koini.api.dto.response.ShiftReportResponse;
import com.koini.api.dto.response.TransactionHistoryResponse;
import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.service.payment.PaymentService;
import com.koini.api.service.merchant.MerchantApprovalService;
import com.koini.api.service.wallet.WalletService;
import com.koini.api.util.SecurityUtils;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.persistence.repository.RouteRepository;
import com.koini.persistence.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/merchant")
@PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
public class MerchantController {

  private final PaymentService paymentService;
  private final MerchantApprovalService merchantApprovalService;
  private final WalletService walletService;
  private final TransactionRepository transactionRepository;
  private final RouteRepository routeRepository;

  public MerchantController(
      PaymentService paymentService,
      MerchantApprovalService merchantApprovalService,
      WalletService walletService,
      TransactionRepository transactionRepository,
      RouteRepository routeRepository
  ) {
    this.paymentService = paymentService;
    this.merchantApprovalService = merchantApprovalService;
    this.walletService = walletService;
    this.transactionRepository = transactionRepository;
    this.routeRepository = routeRepository;
  }

  @Operation(summary = "Redeem payment code", description = "Redeems passenger payment code (merchant)")
  @PostMapping("/payments/redeem")
  public ResponseEntity<ApiResponse<RedeemCodeResponse>> redeem(
      @Valid @RequestBody RedeemCodeRequest request,
      @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId,
      HttpServletRequest httpRequest) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    RedeemCodeResponse response = paymentService.redeemPaymentCode(
        request, SecurityUtils.currentUserId(), httpRequest, idempotencyKey);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Create payment request", description = "Creates a payment request (merchant)")
  @PostMapping("/payments/request")
  public ResponseEntity<ApiResponse<CreateRequestResponse>> createRequest(
      @Valid @RequestBody CreateRequestRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    CreateRequestResponse response = paymentService.createPaymentRequest(request, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Request status", description = "Polls payment request status (merchant)")
  @GetMapping("/payments/request/{id}/status")
  public ResponseEntity<ApiResponse<PaymentRequestStatusResponse>> pollStatus(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    PaymentRequestStatusResponse response = paymentService.pollPaymentRequestStatus(id, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Wallet balance", description = "Returns wallet balance (merchant)")
  @GetMapping("/wallet/balance")
  public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    WalletBalanceResponse response = walletService.getBalance(SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Transaction history", description = "Returns wallet transactions (merchant)")
  @GetMapping("/wallet/transactions")
  public ResponseEntity<ApiResponse<TransactionHistoryResponse>> getTransactions(
      @ModelAttribute TransactionHistoryFilter filter,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    TransactionHistoryResponse response = walletService.getTransactionHistory(SecurityUtils.currentUserId(), filter);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Shift report", description = "Returns today's earnings (merchant)")
  @GetMapping("/shift/report")
  public ResponseEntity<ApiResponse<ShiftReportResponse>> shiftReport(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    long earnings = transactionRepository.sumByTypeTodayForInitiator(
        SecurityUtils.currentUserId(), TransactionType.FARE_PAYMENT);
    ShiftReportResponse response = new ShiftReportResponse(earnings, MoneyUtils.formatUsd(earnings));
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Routes", description = "Returns available routes (merchant)")
  @GetMapping("/routes")
  public ResponseEntity<ApiResponse<List<RouteResponse>>> routes(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    List<RouteResponse> routes = routeRepository.findAll().stream()
        .filter(com.koini.core.domain.entity.Route::isActive)
        .map(route -> new RouteResponse(route.getRouteId().toString(), route.getName(),
            route.getOrigin(), route.getDestination(), route.getFareKc(),
            MoneyUtils.formatUsd(route.getFareKc()), route.isActive()))
        .toList();
    return ResponseEntity.ok(ApiResponse.success(routes, requestId));
  }
}
