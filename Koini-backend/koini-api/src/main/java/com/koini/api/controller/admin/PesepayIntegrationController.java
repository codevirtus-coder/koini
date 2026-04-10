package com.koini.api.controller.admin;

import com.koini.api.dto.request.UpsertPesepayKeysRequest;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.PesepayKeysStatusResponse;
import com.koini.api.service.integration.PesepayConfigService;
import com.koini.core.exception.MisconfigurationException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/integrations/pesepay")
public class PesepayIntegrationController {

  private final PesepayConfigService configService;

  public PesepayIntegrationController(PesepayConfigService configService) {
    this.configService = configService;
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  @Operation(summary = "Pesepay keys status", description = "Returns masked Pesepay integration/encryption keys status")
  public ResponseEntity<ApiResponse<PesepayKeysStatusResponse>> status(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    if (!configService.cryptoConfigured()) {
      PesepayKeysStatusResponse body = new PesepayKeysStatusResponse(false, null, null, null);
      return ResponseEntity.ok(ApiResponse.success(body, requestId));
    }
    Optional<PesepayConfigService.PesepayMaskedStatus> status = configService.getMaskedStatus();
    PesepayKeysStatusResponse body = status
        .map(s -> new PesepayKeysStatusResponse(true, s.integrationKeyMasked(), s.encryptionKeyMasked(), s.updatedAt()))
        .orElseGet(() -> new PesepayKeysStatusResponse(false, null, null, null));
    return ResponseEntity.ok(ApiResponse.success(body, requestId));
  }

  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/keys")
  @Operation(summary = "Upsert Pesepay keys", description = "Stores Pesepay integration and encryption keys (encrypted at rest)")
  public ResponseEntity<ApiResponse<PesepayKeysStatusResponse>> upsert(
      @Valid @RequestBody UpsertPesepayKeysRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    if (!configService.cryptoConfigured()) {
      throw new MisconfigurationException("Missing server config: koini.secrets.master-key-base64",
          "Set KOINI_SECRETS_MASTER_KEY_BASE64 (base64 of 32 random bytes) and restart the API.");
    }

    configService.upsertKeys(request.integrationKey(), request.encryptionKey());
    PesepayConfigService.PesepayMaskedStatus status = configService.getMaskedStatus().orElseThrow();
    PesepayKeysStatusResponse body = new PesepayKeysStatusResponse(true, status.integrationKeyMasked(),
        status.encryptionKeyMasked(), status.updatedAt());
    return ResponseEntity.ok(ApiResponse.success(body, requestId));
  }
}
