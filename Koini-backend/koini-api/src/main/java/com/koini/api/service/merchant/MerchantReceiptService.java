package com.koini.api.service.merchant;

import com.koini.api.dto.response.TransactionReceiptResponse;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.TransactionRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MerchantReceiptService {

  private final TransactionRepository transactionRepository;
  private final MoneyConversionService moneyConversionService;

  public MerchantReceiptService(TransactionRepository transactionRepository, MoneyConversionService moneyConversionService) {
    this.transactionRepository = transactionRepository;
    this.moneyConversionService = moneyConversionService;
  }

  public TransactionReceiptResponse receipt(UUID merchantUserId, UUID transactionId) {
    Transaction tx = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

    boolean allowed = false;
    if (tx.getInitiatedBy() != null && merchantUserId.equals(tx.getInitiatedBy().getUserId())) {
      allowed = true;
    }
    if (!allowed && tx.getFromWallet() != null && tx.getFromWallet().getUser() != null
        && merchantUserId.equals(tx.getFromWallet().getUser().getUserId())) {
      allowed = true;
    }
    if (!allowed && tx.getToWallet() != null && tx.getToWallet().getUser() != null
        && merchantUserId.equals(tx.getToWallet().getUser().getUserId())) {
      allowed = true;
    }
    if (!allowed) {
      throw new ResourceNotFoundException("Transaction not found");
    }

    return new TransactionReceiptResponse(
        tx.getTxId() != null ? tx.getTxId().toString() : null,
        tx.getReference(),
        tx.getTxType() != null ? tx.getTxType().name() : null,
        tx.getStatus() != null ? tx.getStatus().name() : null,
        tx.getAmountKc(),
        moneyConversionService.formatUsd(tx.getAmountKc()),
        tx.getFeeKc(),
        moneyConversionService.formatUsd(tx.getFeeKc()),
        tx.getDescription(),
        tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null
    );
  }
}
