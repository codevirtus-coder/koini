package com.koini.core.domain.valueobject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {

  private static final BigDecimal KC_TO_USD = new BigDecimal("0.01");
  private static final BigDecimal USD_TO_KC = new BigDecimal("100");

  private MoneyUtils() {
  }

  public static String formatUsd(long amountKc) {
    NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
    return format.format(toUsd(amountKc));
  }

  public static BigDecimal toUsd(long amountKc) {
    return BigDecimal.valueOf(amountKc).multiply(KC_TO_USD).setScale(2, RoundingMode.HALF_UP);
  }

  public static long fromUsd(BigDecimal usdAmount) {
    if (usdAmount == null) {
      return 0L;
    }
    return usdAmount.multiply(USD_TO_KC).setScale(0, RoundingMode.HALF_UP).longValueExact();
  }

  public static long calculateFee(long amountKc, double feeRatePercent) {
    if (feeRatePercent <= 0) {
      return 0L;
    }
    BigDecimal rate = BigDecimal.valueOf(feeRatePercent).divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
    return BigDecimal.valueOf(amountKc).multiply(rate).setScale(0, RoundingMode.UP).longValue();
  }

  public static boolean isSufficientBalance(long balance, long required) {
    return balance >= required;
  }
}
