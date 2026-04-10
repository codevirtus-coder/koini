package com.koini.api.service.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.koini.api.config.PesepayProperties;
import com.koini.api.service.money.MoneyConversionService;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

@Service
public class PesepayClient {

  private final PesepayConfigService configService;
  private final PesepayProperties properties;
  private final MoneyConversionService moneyConversionService;
  private final ObjectMapper objectMapper;
  private final RestClient restClient;

  public PesepayClient(PesepayConfigService configService,
      PesepayProperties properties,
      MoneyConversionService moneyConversionService,
      ObjectMapper objectMapper) {
    this.configService = configService;
    this.properties = properties;
    this.moneyConversionService = moneyConversionService;
    this.objectMapper = objectMapper;
    this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
  }

  public PesepayInitiateResponse initiateWalletTopup(long amountKc, String currencyCode, String reasonForPayment,
      String merchantReference, String returnUrl, String resultUrl) {
    PesepayConfigService.PesepayKeys keys = configService.getKeys()
        .orElseThrow(() -> new IllegalStateException("Pesepay keys not configured"));

    Map<String, Object> paymentBody = new LinkedHashMap<>();
    paymentBody.put("amountDetails", Map.of(
        "amount", moneyConversionService.toUsd(amountKc),
        "currencyCode", currencyCode != null && !currencyCode.isBlank() ? currencyCode : properties.getDefaultCurrencyCode()
    ));
    paymentBody.put("reasonForPayment", reasonForPayment);
    paymentBody.put("merchantReference", merchantReference);
    paymentBody.put("returnUrl", returnUrl);
    paymentBody.put("resultUrl", resultUrl);

    String plaintext;
    try {
      plaintext = objectMapper.writeValueAsString(paymentBody);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to serialize Pesepay request", ex);
    }

    String encryptedPayload = encrypt(plaintext, keys.encryptionKey());
    Map<String, String> requestBody = Map.of("payload", encryptedPayload);

    Map<String, Object> apiResponse = restClient.post()
        .uri(properties.getInitiatePath())
        .header("key", keys.integrationKey())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .body(requestBody)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    return parseInitiateResponse(apiResponse, keys.encryptionKey());
  }

  public PesepayCheckResponse checkPayment(String referenceNumber) {
    PesepayConfigService.PesepayKeys keys = configService.getKeys()
        .orElseThrow(() -> new IllegalStateException("Pesepay keys not configured"));

    Map<String, Object> apiResponse = restClient.get()
        .uri(uriBuilder -> uriBuilder.path(properties.getCheckPaymentPath())
            .queryParam("referenceNumber", referenceNumber)
            .build())
        .header("key", keys.integrationKey())
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    return parseCheckResponse(apiResponse, keys.encryptionKey());
  }

  private PesepayInitiateResponse parseInitiateResponse(Map<String, Object> apiResponse, String encryptionKey) {
    if (apiResponse == null || apiResponse.get("payload") == null) {
      throw new IllegalStateException("Invalid Pesepay response");
    }
    String decrypted = decrypt(apiResponse.get("payload").toString(), encryptionKey);
    try {
      Map<String, Object> decoded = objectMapper.readValue(decrypted, new TypeReference<>() {});
      String referenceNumber = value(decoded, "referenceNumber");
      String pollUrl = value(decoded, "pollUrl");
      String redirectUrl = value(decoded, "redirectUrl");
      return new PesepayInitiateResponse(referenceNumber, pollUrl, redirectUrl, decoded);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse Pesepay response", ex);
    }
  }

  private PesepayCheckResponse parseCheckResponse(Map<String, Object> apiResponse, String encryptionKey) {
    if (apiResponse == null || apiResponse.get("payload") == null) {
      throw new IllegalStateException("Invalid Pesepay response");
    }
    String decrypted = decrypt(apiResponse.get("payload").toString(), encryptionKey);
    try {
      Map<String, Object> decoded = objectMapper.readValue(decrypted, new TypeReference<>() {});
      String status = value(decoded, "transactionStatus");
      boolean paid = "SUCCESS".equalsIgnoreCase(status);
      String pollUrl = value(decoded, "pollUrl");
      String referenceNumber = value(decoded, "referenceNumber");
      return new PesepayCheckResponse(referenceNumber, pollUrl, paid, status, decoded);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to parse Pesepay response", ex);
    }
  }

  private String encrypt(String plaintext, String encryptionKey) {
    validatePesepayEncryptionKey(encryptionKey);
    try {
      byte[] ivBytes = encryptionKey.substring(0, 16).getBytes(StandardCharsets.UTF_8);
      IvParameterSpec iv = new IvParameterSpec(ivBytes);
      SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, key, iv);
      byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Failed to encrypt Pesepay payload", ex);
    }
  }

  private String decrypt(String payload, String encryptionKey) {
    validatePesepayEncryptionKey(encryptionKey);
    try {
      byte[] ivBytes = encryptionKey.substring(0, 16).getBytes(StandardCharsets.UTF_8);
      IvParameterSpec iv = new IvParameterSpec(ivBytes);
      SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(StandardCharsets.UTF_8), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, key, iv);
      byte[] decoded = Base64.getDecoder().decode(payload);
      byte[] decrypted = cipher.doFinal(decoded);
      return new String(decrypted, StandardCharsets.UTF_8);
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Failed to decrypt Pesepay payload", ex);
    }
  }

  private void validatePesepayEncryptionKey(String key) {
    if (key == null) {
      throw new IllegalArgumentException("Pesepay encryption key is missing");
    }
    int len = key.length();
    if (len != 16 && len != 24 && len != 32) {
      throw new IllegalArgumentException("Pesepay encryption key must be 16/24/32 characters");
    }
  }

  private String value(Map<String, Object> map, String key) {
    Object v = map.get(key);
    return v != null ? v.toString() : null;
  }

  public record PesepayInitiateResponse(String referenceNumber, String pollUrl, String redirectUrl,
                                       Map<String, Object> raw) {
  }

  public record PesepayCheckResponse(String referenceNumber, String pollUrl, boolean paid, String transactionStatus,
                                    Map<String, Object> raw) {
  }
}
