package com.koini.api.service.auth;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class KoiniUserDetails implements UserDetails {

  private final User user;

  public KoiniUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    UserRole role = user.getRole();
    if (role == UserRole.CONDUCTOR) {
      // Backward compatibility: legacy CONDUCTOR users should be treated as MERCHANT.
      return List.of(
          new SimpleGrantedAuthority("ROLE_" + UserRole.MERCHANT.name()),
          new SimpleGrantedAuthority("ROLE_" + UserRole.CONDUCTOR.name())
      );
    }
    if (role == UserRole.PASSENGER) {
      // Backward compatibility: legacy PASSENGER users should be treated as CLIENT.
      return List.of(
          new SimpleGrantedAuthority("ROLE_" + UserRole.CLIENT.name()),
          new SimpleGrantedAuthority("ROLE_" + UserRole.PASSENGER.name())
      );
    }
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.canonical().name()));
  }

  @Override
  public String getPassword() {
    return user.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return user.getUserId().toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return user.getStatus() != UserStatus.LOCKED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return user.getStatus() == UserStatus.ACTIVE || user.getStatus() == UserStatus.PENDING_VERIFICATION;
  }
}
