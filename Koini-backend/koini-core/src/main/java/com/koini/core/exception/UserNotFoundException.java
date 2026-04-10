package com.koini.core.exception;

public class UserNotFoundException extends BaseKoiniException {
  public static final String ERROR_CODE = "USER_001";

  public UserNotFoundException(String message) {
    super(ERROR_CODE, message);
  }

  public UserNotFoundException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
