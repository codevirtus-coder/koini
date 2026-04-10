package com.koini.core.domain.enums;

public enum UserRole {
  CLIENT,
  /**
   * Legacy role name kept for backward compatibility during rollout.
   * Prefer {@link #CLIENT}.
   */
  PASSENGER,
  MERCHANT,
  /**
   * Legacy role name kept for backward compatibility during rollout.
   * Prefer {@link #MERCHANT}.
   */
  CONDUCTOR,
  AGENT,
  ADMIN,
  FLEET_OWNER
  ;

  public UserRole canonical() {
    if (this == CONDUCTOR) {
      return MERCHANT;
    }
    if (this == PASSENGER) {
      return CLIENT;
    }
    return this;
  }
}
