package com.koini.api.service.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

class ClientRoleCompatibilityTest {

  @Test
  @DisplayName("UserRole canonical mapping: PASSENGER -> CLIENT")
  void canonicalMapping_passengerToClient() {
    assertThat(UserRole.PASSENGER.canonical()).isEqualTo(UserRole.CLIENT);
    assertThat(UserRole.CLIENT.canonical()).isEqualTo(UserRole.CLIENT);
  }

  @Test
  @DisplayName("KoiniUserDetails: legacy PASSENGER gets ROLE_CLIENT authority")
  void legacyPassengerGetsClientAuthority() {
    User user = User.builder().userId(java.util.UUID.randomUUID()).role(UserRole.PASSENGER).build();
    KoiniUserDetails details = new KoiniUserDetails(user);

    Set<String> authorities = details.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(java.util.stream.Collectors.toSet());
    assertThat(authorities).contains("ROLE_CLIENT", "ROLE_PASSENGER");
  }
}
