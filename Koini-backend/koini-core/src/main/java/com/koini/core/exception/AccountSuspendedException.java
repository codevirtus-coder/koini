package com.koini.core.exception;

public class AccountSuspendedException extends BaseKoiniException {
  public static final String ERROR_CODE = "AUTH_005";

  public AccountSuspendedException(String message) {
    super(ERROR_CODE, message);
  }

  public AccountSuspendedException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
