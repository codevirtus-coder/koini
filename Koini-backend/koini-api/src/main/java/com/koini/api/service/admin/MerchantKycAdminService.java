package com.koini.api.service.admin;

import com.koini.api.dto.response.MerchantKycApplicationResponse;
import com.koini.core.domain.entity.MerchantOnboarding;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.MerchantOnboardingRepository;
import com.koini.persistence.repository.UserRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class MerchantKycAdminService {

  public record MerchantDocument(Resource resource, MediaType contentType, String filename) {
  }

  private final UserRepository userRepository;
  private final MerchantOnboardingRepository onboardingRepository;
  private final Path uploadRoot;

  public MerchantKycAdminService(
      UserRepository userRepository,
      MerchantOnboardingRepository onboardingRepository,
      @Value("${koini.upload.dir:uploads}") String uploadDir
  ) {
    this.userRepository = userRepository;
    this.onboardingRepository = onboardingRepository;
    this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
  }

  @PreAuthorize("hasRole('ADMIN')")
  public MerchantKycApplicationResponse getApplication(UUID merchantUserId) {
    User user = userRepository.findById(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() == null || user.getRole().canonical() != UserRole.MERCHANT) {
      throw new ResourceNotFoundException("Merchant not found");
    }

    MerchantOnboarding onboarding = onboardingRepository.findByUserUserId(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Merchant onboarding not submitted"));

    String base = "/api/v1/admin/merchants/" + merchantUserId;
    return new MerchantKycApplicationResponse(
        merchantUserId.toString(),
        user.getStatus() != null ? user.getStatus().name() : null,
        new MerchantKycApplicationResponse.MerchantKycDetail(
            onboarding.getBusinessName(),
            onboarding.getTradingName(),
            onboarding.getAddressLine1(),
            onboarding.getCity(),
            onboarding.getCountry(),
            onboarding.getIdNumber(),
            onboarding.getCreatedAt() != null ? onboarding.getCreatedAt().toString() : null
        ),
        new MerchantKycApplicationResponse.MerchantKycDocuments(
            base + "/documents/idDocument",
            base + "/documents/proofOfAddress"
        )
    );
  }

  @PreAuthorize("hasRole('ADMIN')")
  public MerchantDocument getDocument(UUID merchantUserId, String type) {
    MerchantOnboarding onboarding = onboardingRepository.findByUserUserId(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Merchant onboarding not submitted"));

    String normalized = type != null ? type.trim().toLowerCase(Locale.ROOT) : "";
    String pathString;
    if ("iddocument".equals(normalized) || "id_document".equals(normalized) || "id-document".equals(normalized)) {
      pathString = onboarding.getIdDocumentPath();
    } else if ("proofofaddress".equals(normalized) || "proof_of_address".equals(normalized)
        || "proof-of-address".equals(normalized)) {
      pathString = onboarding.getProofOfAddressPath();
    } else {
      throw new ResourceNotFoundException("Document type not supported");
    }

    Path expectedDir = uploadRoot.resolve("merchant-onboarding").resolve(merchantUserId.toString())
        .toAbsolutePath().normalize();
    Path filePath = Path.of(pathString).toAbsolutePath().normalize();
    if (!filePath.startsWith(expectedDir) || !Files.exists(filePath) || !Files.isRegularFile(filePath)) {
      throw new ResourceNotFoundException("Document not found");
    }

    MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
    try {
      String probed = Files.probeContentType(filePath);
      if (probed != null && !probed.isBlank()) {
        contentType = MediaType.parseMediaType(probed);
      }
    } catch (Exception ignored) {
      // fallback to octet-stream
    }

    Resource resource = new FileSystemResource(filePath);
    return new MerchantDocument(resource, contentType, filePath.getFileName().toString());
  }
}

