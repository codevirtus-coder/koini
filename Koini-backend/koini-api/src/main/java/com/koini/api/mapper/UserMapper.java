package com.koini.api.mapper;

import com.koini.api.dto.response.AdminUserDetailResponse;
import com.koini.api.dto.response.UserSummaryResponse;
import com.koini.api.service.money.MoneyConversionService;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.valueobject.PhoneUtils;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  private final MoneyConversionService moneyConversionService;

  public UserMapper(MoneyConversionService moneyConversionService) {
    this.moneyConversionService = moneyConversionService;
  }

  public UserSummaryResponse toSummary(User user) {
    if (user == null) {
      return null;
    }
    return new UserSummaryResponse(
        user.getUserId() != null ? user.getUserId().toString() : null,
        user.getPhone() != null ? PhoneUtils.mask(user.getPhone()) : null,
        user.getFullName(),
        user.getRole() != null ? user.getRole().canonical().name() : null,
        user.getStatus() != null ? user.getStatus().name() : null
    );
  }

  public AdminUserDetailResponse toAdminDetail(User user, Wallet wallet) {
    AdminUserDetailResponse response = new AdminUserDetailResponse();
    response.setUserId(user.getUserId().toString());
    response.setPhone(user.getPhone());
    response.setFullName(user.getFullName());
    response.setRole(user.getRole().canonical().name());
    response.setStatus(user.getStatus().name());
    response.setKycLevel(String.valueOf(user.getKycLevel()));
    response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
    response.setLastLogin(user.getLastLogin() != null ? user.getLastLogin().toString() : null);
    if (wallet != null) {
      response.setWallet(new AdminUserDetailResponse.WalletSummary(
          wallet.getWalletId().toString(),
          wallet.getBalanceKc(),
          moneyConversionService.formatUsd(wallet.getBalanceKc()),
          wallet.getStatus().name()));
    }
    return response;
  }
}
