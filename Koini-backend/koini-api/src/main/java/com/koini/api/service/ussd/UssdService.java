package com.koini.api.service.ussd;

import com.koini.api.service.auth.AuthService;
import com.koini.core.domain.entity.PaymentCode;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.PaymentCodeStatus;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.core.exception.InsufficientBalanceException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import com.koini.api.service.RedisService;
import com.koini.core.util.KoiniConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UssdService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentCodeRepository paymentCodeRepository;
  private final RedisService redisService;
  private final AuthService authService;
  private final double feeRate;

  public UssdService(
      UserRepository userRepository,
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      PaymentCodeRepository paymentCodeRepository,
      RedisService redisService,
      AuthService authService,
      @Value("${koini.fees.payment-rate}") double feeRate
  ) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.paymentCodeRepository = paymentCodeRepository;
    this.redisService = redisService;
    this.authService = authService;
    this.feeRate = feeRate;
  }

  public String handleSession(String sessionId, String phoneNumber, String text) {
    redisService.set("koini:ussd:" + sessionId, text == null ? "" : text,        Duration.ofSeconds(KoiniConstants.USSD_TTL_SECONDS));
    String[] parts = text == null || text.isBlank() ? new String[0] : text.split("\\*");
    if (parts.length == 0) {
      return "CON 1. Pay fare\n2. Check balance\n3. Send credits\n4. Withdraw cash\n5. Transaction history\n6. Help";
    }
    switch (parts[0]) {
      case "1":
        return handlePayFare(phoneNumber, parts);
      case "2":
        return handleBalance(phoneNumber, parts);
      case "3":
        return handleSendCredits(phoneNumber, parts);
      case "4":
        return "END Withdrawals via USSD are not available. Visit an agent.";
      case "5":
        return "END Transaction history is available in the app.";
      case "6":
        return "END Help: Call support or visit a nearby agent.";
      default:
        return "END Invalid option.";
    }
  }

  private String handlePayFare(String phoneNumber, String[] parts) {
    if (parts.length == 1) {
      return "CON Enter your 4-digit PIN:";
    }
    if (parts.length == 2) {
      return "CON Select amount: 1.$0.30  2.$0.50  3.$1.00  4.Enter amount";
    }
    if (parts.length == 3) {
      String choice = parts[2];
      if ("4".equals(choice)) {
        return "CON Enter amount (KC):";
      }
      long amountKc = amountForChoice(choice);
      return "CON Confirm: Pay " + MoneyUtils.formatUsd(amountKc) + "? 1.Yes  2.No";
    }
    if (parts.length == 4 && "4".equals(parts[2])) {
      long amountKc = Long.parseLong(parts[3]);
      return "CON Confirm: Pay " + MoneyUtils.formatUsd(amountKc) + "? 1.Yes  2.No";
    }
    if (parts.length == 4 && !"4".equals(parts[2])) {
      if ("2".equals(parts[3])) {
        return "END Cancelled.";
      }
      long amountKc = amountForChoice(parts[2]);
      return completePayment(phoneNumber, parts[1], amountKc);
    }
    if (parts.length == 5 && "4".equals(parts[2])) {
      if ("2".equals(parts[4])) {
        return "END Cancelled.";
      }
      long amountKc = Long.parseLong(parts[3]);
      return completePayment(phoneNumber, parts[1], amountKc);
    }
    return "END Invalid input.";
  }

  private String handleBalance(String phoneNumber, String[] parts) {
    if (parts.length == 1) {
      return "CON Enter PIN:";
    }
    String pin = parts[1];
    User user = findUser(phoneNumber);
    authService.verifyPin(pin, user.getUserId());
    Wallet wallet = walletRepository.findByUserUserId(user.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    String lastDate = transactionRepository
        .findByFromWalletOrToWalletOrderByCreatedAtDesc(wallet, wallet, PageRequest.of(0, 1))
        .stream().findFirst()
        .map(tx -> tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : "-")
        .orElse("-");
    return "END Balance: " + wallet.getBalanceKc() + " KC (" + MoneyUtils.formatUsd(wallet.getBalanceKc())
        + "). Last: " + lastDate;
  }

  private String handleSendCredits(String phoneNumber, String[] parts) {
    if (parts.length == 1) {
      return "CON Enter recipient phone:";
    }
    if (parts.length == 2) {
      return "CON Enter amount (KC):";
    }
    if (parts.length == 3) {
      return "CON Enter PIN:";
    }
    if (parts.length == 4) {
      String recipient = parts[1];
      long amount = Long.parseLong(parts[2]);
      String pin = parts[3];
      return completeTransfer(phoneNumber, recipient, amount, pin);
    }
    return "END Invalid input.";
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  protected String completePayment(String phoneNumber, String pin, long amountKc) {
    User user = findUser(phoneNumber);
    authService.verifyPin(pin, user.getUserId());
    Wallet wallet = walletRepository.findByUserIdForUpdate(user.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    long fee = MoneyUtils.calculateFee(amountKc, feeRate);
    if (wallet.getBalanceKc() < amountKc + fee) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    String code = String.valueOf(new java.security.SecureRandom().nextInt(900000) + 100000);
    PaymentCode paymentCode = PaymentCode.builder()
        .codeHash(org.springframework.security.crypto.bcrypt.BCrypt.hashpw(code,
            org.springframework.security.crypto.bcrypt.BCrypt.gensalt(12)))
        .holder(user)
        .amountKc(amountKc)
        .feeKc(fee)
        .status(PaymentCodeStatus.PENDING)
        .expiresAt(LocalDateTime.now().plusSeconds(KoiniConstants.PAYMENT_CODE_TTL_SECONDS))
        .build();
    paymentCodeRepository.save(paymentCode);
    redisService.set("koini:code:" + code, paymentCode.getCodeId().toString(),        Duration.ofSeconds(KoiniConstants.PAYMENT_CODE_TTL_SECONDS));
    return "END Your code: " + code + ". Show merchant. Expires 90s.";
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  protected String completeTransfer(String fromPhone, String toPhone, long amountKc, String pin) {
    User fromUser = findUser(fromPhone);
    User toUser = findUser(toPhone);
    authService.verifyPin(pin, fromUser.getUserId());
    Wallet fromWallet = walletRepository.findByUserIdForUpdate(fromUser.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    Wallet toWallet = walletRepository.findByUserIdForUpdate(toUser.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    if (fromWallet.getBalanceKc() < amountKc) {
      throw new InsufficientBalanceException("Insufficient balance");
    }
    fromWallet.setBalanceKc(fromWallet.getBalanceKc() - amountKc);
    toWallet.setBalanceKc(toWallet.getBalanceKc() + amountKc);
    String reference = "TR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4);
    Transaction tx = Transaction.builder()
        .txType(TransactionType.TRANSFER)
        .fromWallet(fromWallet)
        .toWallet(toWallet)
        .amountKc(amountKc)
        .feeKc(0)
        .status(TransactionStatus.COMPLETED)
        .reference(reference)
        .initiatedBy(fromUser)
        .description("USSD transfer")
        .build();
    transactionRepository.save(tx);
    return "END Sent " + amountKc + " to " + PhoneUtils.mask(toPhone) + ". Ref: " + reference;
  }

  private User findUser(String phone) {
    String normalized = PhoneUtils.normalize(phone);
    return userRepository.findByPhone(normalized)
        .filter(user -> user.getRole().canonical() == UserRole.CLIENT)
        .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
  }

  private long amountForChoice(String choice) {
    return switch (choice) {
      case "1" -> 30;
      case "2" -> 50;
      case "3" -> 100;
      default -> 0;
    };
  }
}
