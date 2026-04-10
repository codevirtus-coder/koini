package com.koini.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "koini.jwt")
public class JwtProperties {

  private String secret;
  private long accessTokenTtlMs;
  private long refreshTokenTtlMs;

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }

  public long getAccessTokenTtlMs() {
    return accessTokenTtlMs;
  }

  public void setAccessTokenTtlMs(long accessTokenTtlMs) {
    this.accessTokenTtlMs = accessTokenTtlMs;
  }

  public long getRefreshTokenTtlMs() {
    return refreshTokenTtlMs;
  }

  public void setRefreshTokenTtlMs(long refreshTokenTtlMs) {
    this.refreshTokenTtlMs = refreshTokenTtlMs;
  }
}
