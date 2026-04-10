package com.koini.core.exception;

public class ResourceNotFoundException extends BaseKoiniException {
  public static final String ERROR_CODE = "RES_001";

  public ResourceNotFoundException(String message) {
    super(ERROR_CODE, message);
  }

  public ResourceNotFoundException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
