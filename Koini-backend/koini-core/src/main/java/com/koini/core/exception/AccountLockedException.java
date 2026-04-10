package com.koini.core.exception;

public class AccountLockedException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_004";

  public AccountLockedException(String message) {
    super(ERROR_CODE, message);
  }

  public AccountLockedException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
