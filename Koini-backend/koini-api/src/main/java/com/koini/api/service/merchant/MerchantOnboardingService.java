package com.koini.api.service.merchant;

import com.koini.api.dto.response.MerchantOnboardingSubmitResponse;
import com.koini.api.service.storage.FileStorageService;
import com.koini.core.domain.entity.MerchantOnboarding;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.MerchantOnboardingRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.api.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MerchantOnboardingService {

  private final UserRepository userRepository;
  private final MerchantOnboardingRepository onboardingRepository;
  private final FileStorageService fileStorageService;
  private final AuditService auditService;

  public MerchantOnboardingService(
      UserRepository userRepository,
      MerchantOnboardingRepository onboardingRepository,
      FileStorageService fileStorageService,
      AuditService auditService
  ) {
    this.userRepository = userRepository;
    this.onboardingRepository = onboardingRepository;
    this.fileStorageService = fileStorageService;
    this.auditService = auditService;
  }

  @Transactional
  @PreAuthorize("hasAnyRole('MERCHANT','CONDUCTOR')")
  public MerchantOnboardingSubmitResponse submit(
      UUID merchantUserId,
      String businessName,
      String tradingName,
      String addressLine1,
      String city,
      String country,
      String idNumber,
      MultipartFile idDocument,
      MultipartFile proofOfAddress,
      HttpServletRequest httpRequest
  ) {
    User user = userRepository.findById(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    UserRole canonicalRole = user.getRole() != null ? user.getRole().canonical() : null;
    if (canonicalRole != UserRole.MERCHANT) {
      throw new ResourceNotFoundException("Merchant not found");
    }

    String idDocPath = fileStorageService.storeMerchantOnboardingFile(merchantUserId, "idDocument", idDocument);
    String proofPath =
        fileStorageService.storeMerchantOnboardingFile(merchantUserId, "proofOfAddress", proofOfAddress);

    MerchantOnboarding onboarding = onboardingRepository.findByUserUserId(merchantUserId)
        .orElseGet(() -> MerchantOnboarding.builder().user(user).build());

    onboarding.setBusinessName(businessName);
    onboarding.setTradingName(tradingName);
    onboarding.setAddressLine1(addressLine1);
    onboarding.setCity(city);
    onboarding.setCountry(country);
    onboarding.setIdNumber(idNumber);
    onboarding.setIdDocumentPath(idDocPath);
    onboarding.setProofOfAddressPath(proofPath);
    onboardingRepository.save(onboarding);

    if (user.getStatus() == null) {
      user.setStatus(UserStatus.PENDING_VERIFICATION);
    }
    auditService.log("MERCHANT_ONBOARDING_SUBMITTED", merchantUserId, user.getRole().canonical().name(),
        "MerchantOnboarding", onboarding.getOnboardingId() != null ? onboarding.getOnboardingId().toString() : null,
        null, onboarding, AuditOutcome.SUCCESS, httpRequest);

    return new MerchantOnboardingSubmitResponse(true,
        user.getStatus() != null ? user.getStatus().name() : UserStatus.PENDING_VERIFICATION.name());
  }
}

