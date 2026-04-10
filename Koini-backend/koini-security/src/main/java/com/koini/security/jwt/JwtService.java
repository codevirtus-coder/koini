package com.koini.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final JwtProperties properties;
  private final SecureRandom secureRandom = new SecureRandom();

  public JwtService(JwtProperties properties) {
    this.properties = properties;
  }

  public String generateAccessToken(UserDetails userDetails, Map<String, Object> extraClaims) {
    Instant now = Instant.now();
    Instant expiry = now.plusMillis(properties.getAccessTokenTtlMs());
    return Jwts.builder()
        .claims(extraClaims)
        .subject(userDetails.getUsername())
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
  }

  public String generateRefreshToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public boolean validateAccessToken(String token) {
    try {
      Claims claims = parseClaims(token);
      return !isTokenExpired(claims);
    } catch (Exception ex) {
      return false;
    }
  }

  public String extractUserId(String token) {
    return parseClaims(token).getSubject();
  }

  public String extractRole(String token) {
    Object role = parseClaims(token).get("role");
    return role != null ? role.toString() : null;
  }

  public boolean isTokenExpired(String token) {
    return isTokenExpired(parseClaims(token));
  }

  private boolean isTokenExpired(Claims claims) {
    return claims.getExpiration() != null && claims.getExpiration().before(new Date());
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private SecretKey getSigningKey() {
    String secret = properties.getSecret();
    if (secret == null || secret.length() < 64) {
      throw new IllegalStateException("JWT secret must be at least 64 characters for HS512");
    }
    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }
}
