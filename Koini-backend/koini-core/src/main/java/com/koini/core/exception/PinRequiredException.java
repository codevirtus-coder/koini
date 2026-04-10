package com.koini.core.exception;

public class PinRequiredException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_006";

  public PinRequiredException(String message) {
    super(ERROR_CODE, message);
  }

  public PinRequiredException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
