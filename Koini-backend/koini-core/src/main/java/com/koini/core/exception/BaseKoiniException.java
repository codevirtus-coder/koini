package com.koini.core.exception;

public abstract class BaseKoiniException extends RuntimeException {

  private final String errorCode;
  private final String details;

  protected BaseKoiniException(String errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.details = null;
  }

  protected BaseKoiniException(String errorCode, String message, String details) {
    super(message);
    this.errorCode = errorCode;
    this.details = details;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getDetails() {
    return details;
  }
}
