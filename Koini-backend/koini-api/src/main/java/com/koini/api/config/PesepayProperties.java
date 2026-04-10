package com.koini.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "koini.pesepay")
public class PesepayProperties {

  private String baseUrl = "https://api.pesepay.com/api/payments-engine/v1";
  private String initiatePath = "/payments/initiate";
  private String checkPaymentPath = "/payments/check-payment";
  private String returnUrl;
  private String resultUrl;
  private String defaultCurrencyCode = "USD";

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getInitiatePath() {
    return initiatePath;
  }

  public void setInitiatePath(String initiatePath) {
    this.initiatePath = initiatePath;
  }

  public String getCheckPaymentPath() {
    return checkPaymentPath;
  }

  public void setCheckPaymentPath(String checkPaymentPath) {
    this.checkPaymentPath = checkPaymentPath;
  }

  public String getReturnUrl() {
    return returnUrl;
  }

  public void setReturnUrl(String returnUrl) {
    this.returnUrl = returnUrl;
  }

  public String getResultUrl() {
    return resultUrl;
  }

  public void setResultUrl(String resultUrl) {
    this.resultUrl = resultUrl;
  }

  public String getDefaultCurrencyCode() {
    return defaultCurrencyCode;
  }

  public void setDefaultCurrencyCode(String defaultCurrencyCode) {
    this.defaultCurrencyCode = defaultCurrencyCode;
  }
}

