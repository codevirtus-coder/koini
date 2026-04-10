package com.koini.api.mapper;

import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.valueobject.MoneyUtils;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

  public WalletBalanceResponse toBalanceResponse(Wallet wallet) {
    if (wallet == null) {
      return null;
    }
    return new WalletBalanceResponse(
        wallet.getWalletId() != null ? wallet.getWalletId().toString() : null,
        wallet.getBalanceKc(),
        wallet.getPoints(),
        MoneyUtils.formatUsd(wallet.getBalanceKc()),
        wallet.getStatus() != null ? wallet.getStatus().name() : null,
        wallet.getUpdatedAt() != null ? wallet.getUpdatedAt().toString() : null
    );
  }
}

