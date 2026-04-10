package com.koini.api.controller.passenger;

import com.koini.api.dto.request.GenerateCodeRequest;
import com.koini.api.dto.request.TransactionHistoryFilter;
import com.koini.api.dto.request.TransferRequest;
import com.koini.api.dto.request.UpdateProfileRequest;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.GenerateCodeResponse;
import com.koini.api.dto.response.TransactionHistoryResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.dto.response.TransferResponse;
import com.koini.api.dto.response.UserSummaryResponse;
import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.mapper.UserMapper;
import com.koini.api.service.payment.PaymentService;
import com.koini.api.service.wallet.WalletService;
import com.koini.api.util.SecurityUtils;
import com.koini.core.domain.entity.User;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.UserRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/passenger")
@Hidden
public class PassengerController {

  private final WalletService walletService;
  private final PaymentService paymentService;
  private final UserRepository userRepository;
  private final UserMapper userMapper;

  public PassengerController(
      WalletService walletService,
      PaymentService paymentService,
      UserRepository userRepository,
      UserMapper userMapper
  ) {
    this.walletService = walletService;
    this.paymentService = paymentService;
    this.userRepository = userRepository;
    this.userMapper = userMapper;
  }

  @Operation(summary = "Wallet balance", description = "Returns wallet balance")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
  })
  @GetMapping("/wallet/balance")
  public ResponseEntity<ApiResponse<WalletBalanceResponse>> getBalance(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    WalletBalanceResponse response = walletService.getBalance(SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Transaction history", description = "Returns wallet transactions")
  @GetMapping("/wallet/transactions")
  public ResponseEntity<ApiResponse<TransactionHistoryResponse>> getTransactions(
      @ModelAttribute TransactionHistoryFilter filter,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    TransactionHistoryResponse response = walletService.getTransactionHistory(SecurityUtils.currentUserId(), filter);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Transfer credits", description = "Transfers credits to another passenger")
  @PostMapping("/wallet/transfer")
  public ResponseEntity<ApiResponse<TransferResponse>> transfer(
      @Valid @RequestBody TransferRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    TransferResponse response = walletService.transfer(request, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Generate payment code", description = "Generates a payment code")
  @PostMapping("/payments/generate-code")
  public ResponseEntity<ApiResponse<GenerateCodeResponse>> generateCode(
      @Valid @RequestBody GenerateCodeRequest request,
      @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    GenerateCodeResponse response = paymentService.generatePaymentCode(
        request, SecurityUtils.currentUserId(), idempotencyKey);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Approve payment request", description = "Approves a merchant request")
  @PostMapping("/payments/request/{requestId}/approve")
  public ResponseEntity<ApiResponse<TransactionResponse>> approveRequest(
      @PathVariable("requestId") String requestId,
      @Valid @RequestBody com.koini.api.dto.request.ApproveRequestRequest body,
      @RequestHeader(value = "X-Request-ID", required = false) String requestIdHeader) {
    com.koini.api.dto.request.ApproveRequestRequest request =
        new com.koini.api.dto.request.ApproveRequestRequest(requestId, body.pin());
    TransactionResponse response = paymentService.approvePaymentRequest(request, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(response, requestIdHeader));
  }

  @Operation(summary = "Decline payment request", description = "Declines a merchant request")
  @PostMapping("/payments/request/{requestId}/decline")
  public ResponseEntity<ApiResponse<Map<String, String>>> declineRequest(
      @PathVariable("requestId") String requestId,
      @RequestHeader(value = "X-Request-ID", required = false) String requestIdHeader) {
    paymentService.declinePaymentRequest(requestId, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "declined"), requestIdHeader));
  }

  @Operation(summary = "Profile", description = "Returns passenger profile")
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> profile(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    User user = userRepository.findById(SecurityUtils.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return ResponseEntity.ok(ApiResponse.success(userMapper.toSummary(user), requestId));
  }

  @Operation(summary = "Update profile", description = "Updates passenger profile")
  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<UserSummaryResponse>> updateProfile(
      @Valid @RequestBody UpdateProfileRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    User user = userRepository.findById(SecurityUtils.currentUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setFullName(request.fullName());
    userRepository.save(user);
    return ResponseEntity.ok(ApiResponse.success(userMapper.toSummary(user), requestId));
  }
}
