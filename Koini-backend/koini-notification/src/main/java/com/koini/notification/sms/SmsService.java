package com.koini.notification.sms;

public interface SmsService {
  void sendTopUpConfirmation(String phone, long amountKc, long newBalanceKc);

  void sendPaymentReceipt(String phone, long amountKc, String reference);

  void sendPaymentRequest(String phone, long amountKc, String merchantInfo);

  void sendWithdrawalConfirmation(String phone, long amountKc, String reference);

  void sendPinLockAlert(String phone, int minutesLocked);

  void sendVerificationOtp(String phone, String otp);

  void sendGenericAlert(String phone, String message);
}
