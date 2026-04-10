package com.koini.core.util;

public final class KoiniConstants {

  public static final int PIN_MAX_ATTEMPTS = 3;
  public static final int PIN_LOCK_MINUTES = 30;
  public static final int LOGIN_MAX_ATTEMPTS = 5;
  public static final int LOGIN_WINDOW_MINUTES = 15;
  public static final int PAYMENT_CODE_TTL_SECONDS = 90;
  public static final int PAYMENT_REQUEST_TTL_SECONDS = 120;
  public static final int PAYMENT_CODE_RATE_LIMIT_PER_HOUR = 20;
  public static final int IDEMPOTENCY_TTL_HOURS = 24;
  public static final int USSD_TTL_SECONDS = 180;
  public static final long WITHDRAW_MIN_KC = 100;
  public static final long WITHDRAW_MAX_KC = 2000;
  public static final long WITHDRAW_DAILY_LIMIT_KC = 5000;

  private KoiniConstants() {
  }
}
