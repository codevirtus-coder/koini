package com.koini.api.service.merchant;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.exception.MerchantNotApprovedException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.UserRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MerchantApprovalService {

  private final UserRepository userRepository;

  public MerchantApprovalService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public void assertApproved(UUID merchantUserId) {
    User user = userRepository.findById(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() == null || user.getRole().canonical() != UserRole.MERCHANT) {
      throw new ResourceNotFoundException("Merchant not found");
    }
    if (user.getStatus() != UserStatus.ACTIVE) {
      throw new MerchantNotApprovedException("Merchant not approved");
    }
  }
}

