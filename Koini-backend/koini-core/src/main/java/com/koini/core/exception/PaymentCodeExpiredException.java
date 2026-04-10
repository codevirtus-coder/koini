package com.koini.core.exception;

public class PaymentCodeExpiredException extends BaseKoiniException {
  public static final String ERROR_CODE = "PAY_001";

  public PaymentCodeExpiredException(String message) {
    super(ERROR_CODE, message);
  }

  public PaymentCodeExpiredException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
