package com.koini.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class MerchantRoleCompatibilityTest {

  @Test
  @DisplayName("UserRole canonical mapping: CONDUCTOR -> MERCHANT")
  void canonicalMapping_conductorToMerchant() {
    assertThat(UserRole.CONDUCTOR.canonical()).isEqualTo(UserRole.MERCHANT);
    assertThat(UserRole.MERCHANT.canonical()).isEqualTo(UserRole.MERCHANT);
  }

  @Test
  @DisplayName("KoiniUserDetails: legacy CONDUCTOR gets ROLE_MERCHANT authority")
  void legacyConductorGetsMerchantAuthority() {
    User user = User.builder().userId(java.util.UUID.randomUUID()).role(UserRole.CONDUCTOR).build();
    KoiniUserDetails details = new KoiniUserDetails(user);

    Set<String> authorities = details.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(java.util.stream.Collectors.toSet());
    assertThat(authorities).contains("ROLE_MERCHANT", "ROLE_CONDUCTOR");
  }
}
