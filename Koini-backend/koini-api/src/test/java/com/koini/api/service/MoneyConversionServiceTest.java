package com.koini.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.koini.api.config.MoneyProperties;
import com.koini.api.service.money.MoneyConversionService;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyConversionServiceTest {

  @Test
  @DisplayName("kc-per-usd test rate: $0.01 => 100 KC; $1.00 => 10000 KC")
  void usdToKc_testRate() {
    MoneyProperties properties = new MoneyProperties();
    properties.setKcPerUsd(10_000);
    MoneyConversionService service = new MoneyConversionService(properties);

    assertThat(service.toKc(new BigDecimal("0.01"))).isEqualTo(100);
    assertThat(service.toKc(new BigDecimal("1.00"))).isEqualTo(10_000);
  }

  @Test
  @DisplayName("kc-per-usd test rate: 100 KC => $0.01")
  void kcToUsd_testRate() {
    MoneyProperties properties = new MoneyProperties();
    properties.setKcPerUsd(10_000);
    MoneyConversionService service = new MoneyConversionService(properties);

    assertThat(service.toUsd(100)).isEqualByComparingTo(new BigDecimal("0.01"));
    assertThat(service.formatUsd(100)).isEqualTo("$0.01");
  }
}
