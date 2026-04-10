package com.koini.api.service.merchant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.exception.MerchantNotApprovedException;
import com.koini.persistence.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MerchantApprovalServiceTest {

  @Mock UserRepository userRepository;

  @Test
  void assertApproved_blocksPendingMerchant() {
    UUID userId = UUID.randomUUID();
    User user = User.builder().userId(userId).role(UserRole.MERCHANT).status(UserStatus.PENDING_VERIFICATION).build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    MerchantApprovalService service = new MerchantApprovalService(userRepository);

    assertThatThrownBy(() -> service.assertApproved(userId))
        .isInstanceOf(MerchantNotApprovedException.class)
        .hasMessageContaining("not approved");
  }
}

