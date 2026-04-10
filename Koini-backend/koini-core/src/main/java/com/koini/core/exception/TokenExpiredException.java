package com.koini.core.exception;

public class TokenExpiredException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_002";

  public TokenExpiredException(String message) {
    super(ERROR_CODE, message);
  }

  public TokenExpiredException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
