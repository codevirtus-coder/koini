package com.koini.core.exception;

public class AuthenticationException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_001";

  public AuthenticationException(String message) {
    super(ERROR_CODE, message);
  }

  public AuthenticationException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
