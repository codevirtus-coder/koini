package com.koini.core.exception;

public class ConcurrentModificationException extends BaseKoiniException {
  public static final String ERROR_CODE = "SYS_001";

  public ConcurrentModificationException(String message) {
    super(ERROR_CODE, message);
  }

  public ConcurrentModificationException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
