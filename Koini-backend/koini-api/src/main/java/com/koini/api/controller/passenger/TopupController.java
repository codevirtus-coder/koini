package com.koini.api.controller.passenger;

import com.koini.api.dto.request.ConfirmPesepayTopupRequest;
import com.koini.api.dto.request.InitiatePesepayTopupRequest;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.service.topup.PesepayTopupService;
import com.koini.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/passenger/topups")
@Hidden
public class TopupController {

  private final PesepayTopupService topupService;

  public TopupController(PesepayTopupService topupService) {
    this.topupService = topupService;
  }

  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @PostMapping("/pesepay/initiate")
  @Operation(summary = "Initiate Pesepay top-up", description = "Creates a pending top-up and returns the Pesepay redirect URL")
  public ResponseEntity<ApiResponse<PesepayTopupService.InitiateTopupResult>> initiate(
      @Valid @RequestBody InitiatePesepayTopupRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    PesepayTopupService.InitiateTopupResult result = topupService.initiate(
        SecurityUtils.currentUserId(), request.amountKc(), request.currencyCode(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success(result, requestId));
  }

  @PreAuthorize("hasAnyRole('CLIENT','PASSENGER')")
  @PostMapping("/pesepay/{txId}/confirm")
  @Operation(summary = "Confirm Pesepay top-up", description = "Checks payment status and credits wallet balance + points if paid")
  public ResponseEntity<ApiResponse<PesepayTopupService.ConfirmTopupResult>> confirm(
      @PathVariable("txId") String txId,
      @RequestBody(required = false) ConfirmPesepayTopupRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    String ref = request != null ? request.referenceNumber() : null;
    PesepayTopupService.ConfirmTopupResult result = topupService.confirm(UUID.fromString(txId), ref, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(result, requestId));
  }
}
