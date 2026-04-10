package com.koini.core.exception;

public class MerchantNotApprovedException extends BaseKoiniException {
  public static final String ERROR_CODE = "MERCHANT_001";

  public MerchantNotApprovedException(String message) {
    super(ERROR_CODE, message);
  }
}

