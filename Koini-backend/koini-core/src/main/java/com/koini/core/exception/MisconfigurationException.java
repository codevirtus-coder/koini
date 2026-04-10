package com.koini.core.exception;

public class MisconfigurationException extends BaseKoiniException {
  public static final String ERROR_CODE = "CONFIG_001";

  public MisconfigurationException(String message) {
    super(ERROR_CODE, message);
  }

  public MisconfigurationException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}

