package com.koini.core.exception;

public class UserAlreadyExistsException extends BaseKoiniException {
  public static final String ERROR_CODE = "USER_002";

  public UserAlreadyExistsException(String message) {
    super(ERROR_CODE, message);
  }

  public UserAlreadyExistsException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
