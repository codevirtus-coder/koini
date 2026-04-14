package com.koini.api.controller.merchant;

import com.koini.api.dto.request.CreateRequestRequest;
import com.koini.api.dto.request.RedeemCodeRequest;
import com.koini.api.dto.request.TransactionHistoryFilter;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.CancelPaymentRequestResponse;
import com.koini.api.dto.response.CreateRequestResponse;
import com.koini.api.dto.response.MerchantDashboardSummaryResponse;
import com.koini.api.dto.response.MerchantPaymentRequestListResponse;
import com.koini.api.dto.response.MerchantPaymentRequestResponse;
import com.koini.api.dto.response.PaymentRequestStatusResponse;
import com.koini.api.dto.response.RedeemCodeResponse;
import com.koini.api.dto.response.RouteResponse;
import com.koini.api.dto.response.ShiftReportResponse;
import com.koini.api.dto.response.TransactionHistoryResponse;
import com.koini.api.dto.response.TransactionReceiptResponse;
import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.service.payment.PaymentService;
import com.koini.api.service.merchant.MerchantApprovalService;
import com.koini.api.service.merchant.MerchantDashboardService;
import com.koini.api.service.merchant.MerchantReceiptService;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.api.service.wallet.WalletService;
import com.koini.api.util.SecurityUtils;
import com.koini.core.domain.enums.PaymentReqStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.persistence.repository.RouteRepository;
import com.koini.persistence.repository.TransactionRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/merchant")
@PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
public class MerchantController {

  private final PaymentService paymentService;
  private final MerchantApprovalService merchantApprovalService;
  private final MerchantDashboardService merchantDashboardService;
  private final MerchantReceiptService merchantReceiptService;
  private final WalletService walletService;
  private final TransactionRepository transactionRepository;
  private final RouteRepository routeRepository;
  private final MoneyConversionService moneyConversionService;

  public MerchantController(
      PaymentService paymentService,
      MerchantApprovalService merchantApprovalService,
      MerchantDashboardService merchantDashboardService,
      MerchantReceiptService merchantReceiptService,
      WalletService walletService,
      TransactionRepository transactionRepository,
      RouteRepository routeRepository,
      MoneyConversionService moneyConversionService
  ) {
    this.paymentService = paymentService;
    this.merchantApprovalService = merchantApprovalService;
    this.merchantDashboardService = merchantDashboardService;
    this.merchantReceiptService = merchantReceiptService;
    this.walletService = walletService;
    this.transactionRepository = transactionRepository;
    this.routeRepository = routeRepository;
    this.moneyConversionService = moneyConversionService;
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
    LocalDateTime start = LocalDate.now().atStartOfDay();
    LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();
    long earnings = transactionRepository.sumByTypeForToWalletBetween(
        SecurityUtils.currentUserId(), TransactionType.FARE_PAYMENT, start, end);
    ShiftReportResponse response = new ShiftReportResponse(earnings, moneyConversionService.formatUsd(earnings));
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
            moneyConversionService.formatUsd(route.getFareKc()), route.isActive()))
        .toList();
    return ResponseEntity.ok(ApiResponse.success(routes, requestId));
  }

  @Operation(summary = "Dashboard summary", description = "One-call merchant dashboard summary")
  @GetMapping("/dashboard/summary")
  public ResponseEntity<ApiResponse<MerchantDashboardSummaryResponse>> dashboardSummary(
      @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    MerchantDashboardSummaryResponse response = merchantDashboardService.summary(SecurityUtils.currentUserId(), date);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "List payment requests", description = "Lists merchant payment requests")
  @GetMapping("/payments/requests")
  public ResponseEntity<ApiResponse<MerchantPaymentRequestListResponse>> listRequests(
      @RequestParam(name = "status", required = false) PaymentReqStatus status,
      @RequestParam(name = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
      @RequestParam(name = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    MerchantPaymentRequestListResponse response = paymentService.listPaymentRequests(
        SecurityUtils.currentUserId(), status, dateFrom, dateTo, page, size);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Get payment request", description = "Returns a payment request (merchant)")
  @GetMapping("/payments/request/{id}")
  public ResponseEntity<ApiResponse<MerchantPaymentRequestResponse>> getRequest(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    MerchantPaymentRequestResponse response = paymentService.getPaymentRequest(SecurityUtils.currentUserId(), id);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Cancel payment request", description = "Cancels a payment request (merchant)")
  @PostMapping("/payments/request/{id}/cancel")
  public ResponseEntity<ApiResponse<CancelPaymentRequestResponse>> cancelRequest(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    CancelPaymentRequestResponse response = paymentService.cancelPaymentRequest(id, SecurityUtils.currentUserId(), idempotencyKey);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Transaction receipt", description = "Returns a JSON receipt for a transaction (merchant)")
  @GetMapping("/transactions/{transactionId}/receipt")
  public ResponseEntity<ApiResponse<TransactionReceiptResponse>> receipt(
      @PathVariable("transactionId") String transactionId,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    merchantApprovalService.assertApproved(SecurityUtils.currentUserId());
    TransactionReceiptResponse response = merchantReceiptService.receipt(SecurityUtils.currentUserId(), UUID.fromString(transactionId));
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }
}
