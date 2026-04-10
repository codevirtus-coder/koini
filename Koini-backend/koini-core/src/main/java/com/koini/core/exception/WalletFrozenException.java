package com.koini.core.exception;

public class WalletFrozenException extends BaseKoiniException {
  public static final String ERROR_CODE = "WALLET_002";

  public WalletFrozenException(String message) {
    super(ERROR_CODE, message);
  }

  public WalletFrozenException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
