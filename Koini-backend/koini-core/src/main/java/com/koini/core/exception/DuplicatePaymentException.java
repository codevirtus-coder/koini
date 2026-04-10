package com.koini.core.exception;

public class DuplicatePaymentException extends BaseKoiniException {
  public static final String ERROR_CODE = "PAY_005";

  public DuplicatePaymentException(String message) {
    super(ERROR_CODE, message);
  }

  public DuplicatePaymentException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
