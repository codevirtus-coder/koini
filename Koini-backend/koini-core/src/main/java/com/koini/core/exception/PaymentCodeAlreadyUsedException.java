package com.koini.core.exception;

public class PaymentCodeAlreadyUsedException extends BaseKoiniException {
  public static final String ERROR_CODE = "PAY_003";

  public PaymentCodeAlreadyUsedException(String message) {
    super(ERROR_CODE, message);
  }

  public PaymentCodeAlreadyUsedException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
