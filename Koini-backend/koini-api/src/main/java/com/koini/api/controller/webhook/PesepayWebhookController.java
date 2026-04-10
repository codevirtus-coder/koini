package com.koini.api.controller.webhook;

import com.koini.api.dto.response.ApiResponse;
import com.koini.api.service.topup.PesepayTopupService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/pesepay")
public class PesepayWebhookController {

  private final PesepayTopupService topupService;

  public PesepayWebhookController(PesepayTopupService topupService) {
    this.topupService = topupService;
  }

  @GetMapping("/return")
  @Operation(summary = "Pesepay return URL", description = "Called by Pesepay after redirect flow; confirms top-up and credits wallet if paid")
  public ResponseEntity<ApiResponse<PesepayTopupService.ConfirmTopupResult>> returnUrl(
      @RequestParam("txId") String txId,
      @RequestParam(value = "referenceNumber", required = false) String referenceNumber,
      HttpServletRequest httpRequest) {
    PesepayTopupService.ConfirmTopupResult result =
        topupService.confirm(UUID.fromString(txId), referenceNumber, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(result, null));
  }

  @PostMapping("/result")
  @Operation(summary = "Pesepay result URL", description = "Server-to-server callback endpoint; confirms top-up and credits wallet if paid")
  public ResponseEntity<ApiResponse<PesepayTopupService.ConfirmTopupResult>> resultUrl(
      @RequestParam("txId") String txId,
      @RequestParam(value = "referenceNumber", required = false) String referenceNumber,
      HttpServletRequest httpRequest) {
    PesepayTopupService.ConfirmTopupResult result =
        topupService.confirm(UUID.fromString(txId), referenceNumber, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(result, null));
  }
}
