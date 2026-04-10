package com.koini.notification.sms;

import com.koini.core.domain.valueobject.MoneyUtils;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AfricasTalkingSmsService implements SmsService {

  private static final Logger log = LoggerFactory.getLogger(AfricasTalkingSmsService.class);

  private final String username;
  private final String apiKey;
  private final String topUpTemplate;
  private final String paymentReceiptTemplate;
  private final String paymentRequestTemplate;
  private final String withdrawalTemplate;
  private final String pinLockTemplate;
  private final String otpTemplate;

  public AfricasTalkingSmsService(
      @Value("${koini.sms.username}") String username,
      @Value("${koini.sms.api-key}") String apiKey,
      @Value("${koini.sms.templates.topup}") String topUpTemplate,
      @Value("${koini.sms.templates.payment-receipt}") String paymentReceiptTemplate,
      @Value("${koini.sms.templates.payment-request}") String paymentRequestTemplate,
      @Value("${koini.sms.templates.withdrawal}") String withdrawalTemplate,
      @Value("${koini.sms.templates.pin-lock}") String pinLockTemplate,
      @Value("${koini.sms.templates.otp}") String otpTemplate) {
    this.username = username;
    this.apiKey = apiKey;
    this.topUpTemplate = topUpTemplate;
    this.paymentReceiptTemplate = paymentReceiptTemplate;
    this.paymentRequestTemplate = paymentRequestTemplate;
    this.withdrawalTemplate = withdrawalTemplate;
    this.pinLockTemplate = pinLockTemplate;
    this.otpTemplate = otpTemplate;
  }

  @Async
  @Override
  public void sendTopUpConfirmation(String phone, long amountKc, long newBalanceKc) {
    String message = topUpTemplate
        .replace("{amount}", MoneyUtils.formatUsd(amountKc))
        .replace("{balance}", MoneyUtils.formatUsd(newBalanceKc));
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendPaymentReceipt(String phone, long amountKc, String reference) {
    String message = paymentReceiptTemplate
        .replace("{amount}", MoneyUtils.formatUsd(amountKc))
        .replace("{reference}", reference);
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendPaymentRequest(String phone, long amountKc, String merchantInfo) {
    String message = paymentRequestTemplate
        .replace("{amount}", MoneyUtils.formatUsd(amountKc))
        .replace("{conductor}", merchantInfo)
        .replace("{merchant}", merchantInfo);
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendWithdrawalConfirmation(String phone, long amountKc, String reference) {
    String message = withdrawalTemplate
        .replace("{amount}", MoneyUtils.formatUsd(amountKc))
        .replace("{reference}", reference);
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendPinLockAlert(String phone, int minutesLocked) {
    String message = pinLockTemplate.replace("{minutes}", String.valueOf(minutesLocked));
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendVerificationOtp(String phone, String otp) {
    String message = otpTemplate.replace("{otp}", otp);
    sendMessage(phone, message);
  }

  @Async
  @Override
  public void sendGenericAlert(String phone, String message) {
    sendMessage(phone, message);
  }

  private void sendMessage(String phone, String message) {
    try {
      Class<?> at = Class.forName("com.africastalking.AfricasTalking");
      Method initialize = at.getMethod("initialize", String.class, String.class);
      initialize.invoke(null, username, apiKey);
      Method getService = at.getMethod("getService", String.class);
      Object smsService = getService.invoke(null, "SMS");
      Method send = smsService.getClass().getMethod("send", String.class, String[].class);
      send.invoke(smsService, message, new String[] { phone });
    } catch (Exception ex) {
      log.error("Failed to send SMS to {}: {}", phone, ex.getMessage());
    }
  }
}
