package com.koini.core.exception;

public class PaymentRequestExpiredException extends BaseKoiniException {
  public static final String ERROR_CODE = "PAY_004";

  public PaymentRequestExpiredException(String message) {
    super(ERROR_CODE, message);
  }

  public PaymentRequestExpiredException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
