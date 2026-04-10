package com.koini.core.exception;

public class WalletNotFoundException extends BaseKoiniException {
  public static final String ERROR_CODE = "WALLET_003";

  public WalletNotFoundException(String message) {
    super(ERROR_CODE, message);
  }

  public WalletNotFoundException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
