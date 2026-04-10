package com.koini.core.exception;

public class AccessDeniedException extends BaseKoiniException {
  public static final String ERROR_CODE = "SEC_001";

  public AccessDeniedException(String message) {
    super(ERROR_CODE, message);
  }

  public AccessDeniedException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
