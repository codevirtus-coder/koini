package com.koini.api.service.integration;

import com.koini.api.service.crypto.SecretsCryptoService;
import com.koini.core.domain.entity.PaymentGatewayConfig;
import com.koini.persistence.repository.PaymentGatewayConfigRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PesepayConfigService {

  public static final String PROVIDER = "PESEPAY";

  private final PaymentGatewayConfigRepository repository;
  private final SecretsCryptoService cryptoService;

  public PesepayConfigService(PaymentGatewayConfigRepository repository, SecretsCryptoService cryptoService) {
    this.repository = repository;
    this.cryptoService = cryptoService;
  }

  public boolean cryptoConfigured() {
    return cryptoService.isConfigured();
  }

  @Transactional
  public PaymentGatewayConfig upsertKeys(String integrationKey, String encryptionKey) {
    String integrationEnc = cryptoService.encryptToString(integrationKey);
    String encryptionEnc = cryptoService.encryptToString(encryptionKey);

    PaymentGatewayConfig existing = repository.findById(PROVIDER).orElse(null);
    PaymentGatewayConfig cfg = existing != null ? existing : new PaymentGatewayConfig();
    cfg.setProvider(PROVIDER);
    cfg.setIntegrationKeyEnc(integrationEnc);
    cfg.setEncryptionKeyEnc(encryptionEnc);
    return repository.save(cfg);
  }

  public Optional<PesepayKeys> getKeys() {
    if (!cryptoService.isConfigured()) {
      return Optional.empty();
    }
    return repository.findById(PROVIDER).map(cfg -> new PesepayKeys(
        cryptoService.decryptFromString(cfg.getIntegrationKeyEnc()),
        cryptoService.decryptFromString(cfg.getEncryptionKeyEnc()),
        cfg.getUpdatedAt() != null ? cfg.getUpdatedAt().toString() : null
    ));
  }

  public Optional<PesepayMaskedStatus> getMaskedStatus() {
    if (!cryptoService.isConfigured()) {
      return Optional.empty();
    }
    return repository.findById(PROVIDER).map(cfg -> new PesepayMaskedStatus(
        true,
        mask(cryptoService.decryptFromString(cfg.getIntegrationKeyEnc())),
        mask(cryptoService.decryptFromString(cfg.getEncryptionKeyEnc())),
        cfg.getUpdatedAt() != null ? cfg.getUpdatedAt().toString() : null
    ));
  }

  private String mask(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.length() <= 10) {
      return "****";
    }
    return trimmed.substring(0, 6) + "****" + trimmed.substring(trimmed.length() - 4);
  }

  public record PesepayKeys(String integrationKey, String encryptionKey, String updatedAt) {
  }

  public record PesepayMaskedStatus(boolean configured, String integrationKeyMasked, String encryptionKeyMasked,
                                   String updatedAt) {
  }
}
