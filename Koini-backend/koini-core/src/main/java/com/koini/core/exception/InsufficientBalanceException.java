package com.koini.core.exception;

public class InsufficientBalanceException extends BaseKoiniException {
  public static final String ERROR_CODE = "WALLET_001";

  public InsufficientBalanceException(String message) {
    super(ERROR_CODE, message);
  }

  public InsufficientBalanceException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
