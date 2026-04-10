package com.koini.api.util;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.koini.core.exception.AuthenticationException;

public final class SecurityUtils {

  private SecurityUtils() {
  }

  public static UUID currentUserId() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated() || auth.getName() == null || "anonymousUser".equals(auth.getName())) {
      throw new AuthenticationException("Authentication required");
    }
    return UUID.fromString(auth.getName());
  }
}
