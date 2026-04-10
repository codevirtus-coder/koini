package com.koini.core.exception;

public class ExternalServiceException extends BaseKoiniException {
  public static final String ERROR_CODE = "SYS_002";

  public ExternalServiceException(String message) {
    super(ERROR_CODE, message);
  }

  public ExternalServiceException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
