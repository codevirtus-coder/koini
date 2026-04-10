package com.koini.api.service.crypto;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecretsCryptoService {

  private static final int GCM_TAG_BITS = 128;
  private static final int GCM_IV_BYTES = 12;

  private final SecureRandom secureRandom = new SecureRandom();
  private final SecretKey masterKey;

  public SecretsCryptoService(@Value("${koini.secrets.master-key-base64:}") String masterKeyBase64) {
    if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
      this.masterKey = null;
      return;
    }
    byte[] decoded = Base64.getDecoder().decode(masterKeyBase64.trim());
    if (decoded.length != 32) {
      throw new IllegalArgumentException("koini.secrets.master-key-base64 must decode to 32 bytes");
    }
    this.masterKey = new SecretKeySpec(decoded, "AES");
  }

  public boolean isConfigured() {
    return masterKey != null;
  }

  public String encryptToString(String plaintext) {
    requireConfigured();
    if (plaintext == null) {
      return null;
    }
    try {
      byte[] iv = new byte[GCM_IV_BYTES];
      secureRandom.nextBytes(iv);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ciphertext);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to encrypt secret", ex);
    }
  }

  public String decryptFromString(String value) {
    requireConfigured();
    if (value == null) {
      return null;
    }
    String[] parts = value.split(":", 2);
    if (parts.length != 2) {
      throw new IllegalArgumentException("Encrypted value has invalid format");
    }
    try {
      byte[] iv = Base64.getDecoder().decode(parts[0]);
      byte[] ciphertext = Base64.getDecoder().decode(parts[1]);
      Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
      cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
      byte[] plaintext = cipher.doFinal(ciphertext);
      return new String(plaintext, StandardCharsets.UTF_8);
    } catch (Exception ex) {
      throw new IllegalStateException("Failed to decrypt secret", ex);
    }
  }

  private void requireConfigured() {
    if (!isConfigured()) {
      throw new IllegalStateException("Secrets crypto not configured (set koini.secrets.master-key-base64)");
    }
  }
}

