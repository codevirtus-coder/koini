package com.koini.api.mapper;

import com.koini.api.dto.response.WalletBalanceResponse;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.core.domain.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

  private final MoneyConversionService moneyConversionService;

  public WalletMapper(MoneyConversionService moneyConversionService) {
    this.moneyConversionService = moneyConversionService;
  }

  public WalletBalanceResponse toBalanceResponse(Wallet wallet) {
    if (wallet == null) {
      return null;
    }
    return new WalletBalanceResponse(
        wallet.getWalletId() != null ? wallet.getWalletId().toString() : null,
        wallet.getBalanceKc(),
        wallet.getPoints(),
        moneyConversionService.formatUsd(wallet.getBalanceKc()),
        wallet.getStatus() != null ? wallet.getStatus().name() : null,
        wallet.getUpdatedAt() != null ? wallet.getUpdatedAt().toString() : null
    );
  }
}
