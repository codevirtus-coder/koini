package com.koini.api.controller.merchant;

import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.MerchantOnboardingSubmitResponse;
import com.koini.api.service.merchant.MerchantOnboardingService;
import com.koini.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/merchant")
@PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
public class MerchantOnboardingController {

  private final MerchantOnboardingService onboardingService;

  public MerchantOnboardingController(MerchantOnboardingService onboardingService) {
    this.onboardingService = onboardingService;
  }

  @Operation(summary = "Merchant onboarding", description = "Submit merchant KYC onboarding docs (multipart)")
  @PostMapping(value = "/onboarding", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<MerchantOnboardingSubmitResponse>> submit(
      @RequestParam(name = "businessName") String businessName,
      @RequestParam(name = "tradingName") String tradingName,
      @RequestParam(name = "addressLine1") String addressLine1,
      @RequestParam(name = "city") String city,
      @RequestParam(name = "country") String country,
      @RequestParam(name = "idNumber") String idNumber,
      @RequestPart(name = "idDocument") MultipartFile idDocument,
      @RequestPart(name = "proofOfAddress") MultipartFile proofOfAddress,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId
  ) {
    MerchantOnboardingSubmitResponse response = onboardingService.submit(
        SecurityUtils.currentUserId(),
        businessName,
        tradingName,
        addressLine1,
        city,
        country,
        idNumber,
        idDocument,
        proofOfAddress,
        httpRequest
    );
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }
}

