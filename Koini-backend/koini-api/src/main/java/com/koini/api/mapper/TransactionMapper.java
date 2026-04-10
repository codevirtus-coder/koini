package com.koini.api.mapper;

import com.koini.api.dto.response.TransactionResponse;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.valueobject.MoneyUtils;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

  public TransactionResponse toResponse(Transaction transaction) {
    if (transaction == null) {
      return null;
    }
    return new TransactionResponse(
        transaction.getTxId() != null ? transaction.getTxId().toString() : null,
        transaction.getTxType() != null ? transaction.getTxType().name() : null,
        transaction.getStatus() != null ? transaction.getStatus().name() : null,
        transaction.getAmountKc(),
        MoneyUtils.formatUsd(transaction.getAmountKc()),
        transaction.getFeeKc(),
        MoneyUtils.formatUsd(transaction.getFeeKc()),
        transaction.getReference(),
        transaction.getCreatedAt() != null ? transaction.getCreatedAt().toString() : null
    );
  }

  public List<TransactionResponse> toResponseList(List<Transaction> transactions) {
    if (transactions == null || transactions.isEmpty()) {
      return Collections.emptyList();
    }
    return transactions.stream().map(this::toResponse).toList();
  }
}

