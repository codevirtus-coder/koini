package com.koini.core.exception;

public class TokenInvalidException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_003";

  public TokenInvalidException(String message) {
    super(ERROR_CODE, message);
  }

  public TokenInvalidException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
