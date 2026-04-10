package com.koini.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "koini.money")
public class MoneyProperties {

  private long kcPerUsd = 100;

  public long getKcPerUsd() {
    return kcPerUsd;
  }

  public void setKcPerUsd(long kcPerUsd) {
    this.kcPerUsd = kcPerUsd;
  }
}

