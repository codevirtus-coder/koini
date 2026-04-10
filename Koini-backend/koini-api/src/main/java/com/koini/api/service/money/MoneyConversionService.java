package com.koini.api.service.money;

import com.koini.api.config.MoneyProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class MoneyConversionService {

  private final MoneyProperties properties;

  public MoneyConversionService(MoneyProperties properties) {
    this.properties = properties;
  }

  public long kcPerUsd() {
    return properties.getKcPerUsd();
  }

  public BigDecimal toUsd(long amountKc) {
    return toUsd(amountKc, properties.getKcPerUsd());
  }

  public BigDecimal toUsd(long amountKc, long kcPerUsd) {
    if (kcPerUsd <= 0) {
      throw new IllegalStateException("Invalid conversion rate: koini.money.kc-per-usd must be > 0");
    }
    return BigDecimal.valueOf(amountKc).divide(BigDecimal.valueOf(kcPerUsd), 2, RoundingMode.HALF_UP);
  }

  public long toKc(BigDecimal amountUsd) {
    return toKc(amountUsd, properties.getKcPerUsd());
  }

  public long toKc(BigDecimal amountUsd, long kcPerUsd) {
    if (kcPerUsd <= 0) {
      throw new IllegalStateException("Invalid conversion rate: koini.money.kc-per-usd must be > 0");
    }
    if (amountUsd == null) {
      return 0L;
    }
    return amountUsd
        .multiply(BigDecimal.valueOf(kcPerUsd))
        .setScale(0, RoundingMode.HALF_UP)
        .longValue();
  }
}
