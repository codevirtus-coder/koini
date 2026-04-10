package com.koini.core.exception;

public class PaymentCodeInvalidException extends BaseKoiniException {
  public static final String ERROR_CODE = "PAY_002";

  public PaymentCodeInvalidException(String message) {
    super(ERROR_CODE, message);
  }

  public PaymentCodeInvalidException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
