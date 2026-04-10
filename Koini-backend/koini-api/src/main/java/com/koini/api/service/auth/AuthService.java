package com.koini.api.service.auth;

import com.koini.api.dto.request.ChangePinRequest;
import com.koini.api.dto.request.LoginRequest;
import com.koini.api.dto.request.RefreshTokenRequest;
import com.koini.api.dto.request.RegisterAgentRequest;
import com.koini.api.dto.request.RegisterConductorRequest;
import com.koini.api.dto.request.RegisterMerchantRequest;
import com.koini.api.dto.request.RegisterMerchantSelfRequest;
import com.koini.api.dto.request.RegisterPassengerRequest;
import com.koini.api.dto.request.SetupPinRequest;
import com.koini.api.dto.response.LoginResponse;
import com.koini.api.dto.response.RegisterResponse;
import com.koini.api.dto.response.UserSummaryResponse;
import com.koini.api.mapper.UserMapper;
import com.koini.api.service.AuditService;
import com.koini.api.util.TokenUtils;
import com.koini.core.domain.entity.Agent;
import com.koini.core.domain.entity.RefreshToken;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.domain.enums.WalletStatus;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.core.exception.AccountLockedException;
import com.koini.core.exception.AccountSuspendedException;
import com.koini.core.exception.AuthenticationException;
import com.koini.core.exception.PinRequiredException;
import com.koini.core.exception.UserAlreadyExistsException;
import com.koini.core.exception.UserNotFoundException;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.RefreshTokenRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import com.koini.security.jwt.JwtService;
import com.koini.core.util.KoiniConstants;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final AgentRepository agentRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RateLimitService rateLimitService;
  private final SmsService smsService;
  private final AuditService auditService;
  private final UserMapper userMapper;

  public AuthService(
      UserRepository userRepository,
      WalletRepository walletRepository,
      AgentRepository agentRepository,
      RefreshTokenRepository refreshTokenRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      RateLimitService rateLimitService,
      SmsService smsService,
      AuditService auditService,
      UserMapper userMapper
  ) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.agentRepository = agentRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.rateLimitService = rateLimitService;
    this.smsService = smsService;
    this.auditService = auditService;
    this.userMapper = userMapper;
  }

  /**
   * Registers a new client user and creates an associated wallet.
   */
  @Transactional
  public RegisterResponse registerPassenger(RegisterPassengerRequest request, HttpServletRequest httpRequest) {
    String normalized = PhoneUtils.normalize(request.phone());
    if (userRepository.existsByPhone(normalized)) {
      throw new UserAlreadyExistsException("Phone already registered");
    }
    User user = User.builder()
        .phone(normalized)
        .fullName(request.fullName())
        .passwordHash(passwordEncoder.encode(request.password()))
        .role(UserRole.CLIENT)
        .status(UserStatus.PENDING_VERIFICATION)
        .pinAttempts((short) 0)
        .kycLevel((short) 0)
        .build();
    userRepository.save(user);
    Wallet wallet = Wallet.builder()
        .user(user)
        .balanceKc(0)
        .status(WalletStatus.ACTIVE)
        .build();
    walletRepository.save(wallet);
    smsService.sendVerificationOtp(user.getPhone(), "0000");
    auditService.log("USER_REGISTERED", user.getUserId(), user.getRole().name(),
        "User", user.getUserId().toString(), null, user, AuditOutcome.SUCCESS, httpRequest);
    return new RegisterResponse(user.getUserId().toString(), PhoneUtils.mask(user.getPhone()),
        "Registration successful");
  }

  /**
   * Registers a new merchant user (self-service) and creates an associated wallet.
   * Merchant is created with {@link UserStatus#PENDING_VERIFICATION} and must be approved by admin before money actions.
   */
  @Transactional
  public RegisterResponse registerMerchantSelf(RegisterMerchantSelfRequest request, HttpServletRequest httpRequest) {
    String normalized = PhoneUtils.normalize(request.phone());
    if (userRepository.existsByPhone(normalized)) {
      throw new UserAlreadyExistsException("Phone already registered");
    }
    User user = User.builder()
        .phone(normalized)
        .fullName(request.fullName())
        .passwordHash(passwordEncoder.encode(request.password()))
        .role(UserRole.MERCHANT)
        .status(UserStatus.PENDING_VERIFICATION)
        .pinAttempts((short) 0)
        .kycLevel((short) 0)
        .build();
    userRepository.save(user);
    walletRepository.save(Wallet.builder()
        .user(user)
        .balanceKc(0)
        .status(WalletStatus.ACTIVE)
        .build());
    smsService.sendVerificationOtp(user.getPhone(), "0000");
    auditService.log("MERCHANT_SELF_REGISTERED", user.getUserId(), user.getRole().name(),
        "User", user.getUserId().toString(), null, user, AuditOutcome.SUCCESS, httpRequest);
    return new RegisterResponse(user.getUserId().toString(), PhoneUtils.mask(user.getPhone()),
        "Registration successful");
  }

  /**
   * Registers a new merchant user (admin-only).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public RegisterResponse registerMerchant(RegisterMerchantRequest request, HttpServletRequest httpRequest) {
    String normalized = PhoneUtils.normalize(request.phone());
    if (userRepository.existsByPhone(normalized)) {
      throw new UserAlreadyExistsException("Phone already registered");
    }
    String tempPin = generatePin();
    User user = User.builder()
        .phone(normalized)
        .fullName(request.fullName())
        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
        .pinHash(passwordEncoder.encode(tempPin))
        .role(UserRole.MERCHANT)
        .status(UserStatus.ACTIVE)
        .pinAttempts((short) 0)
        .kycLevel((short) 0)
        .build();
    userRepository.save(user);
    walletRepository.save(Wallet.builder()
        .user(user)
        .balanceKc(0)
        .status(WalletStatus.ACTIVE)
        .build());
    smsService.sendGenericAlert(user.getPhone(), "Your temporary PIN is " + tempPin);
    auditService.log("MERCHANT_REGISTERED", user.getUserId(), "ADMIN",
        "User", user.getUserId().toString(), null, user, AuditOutcome.SUCCESS, httpRequest);
    return new RegisterResponse(user.getUserId().toString(), PhoneUtils.mask(user.getPhone()),
        "Merchant registered");
  }

  /**
   * Legacy alias kept during rollout: registers a merchant user.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public RegisterResponse registerConductor(RegisterConductorRequest request, HttpServletRequest httpRequest) {
    return registerMerchant(new RegisterMerchantRequest(request.phone(), request.fullName(), request.routeId()),
        httpRequest);
  }

  /**
   * Registers a new agent user (admin-only).
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public RegisterResponse registerAgent(RegisterAgentRequest request, HttpServletRequest httpRequest) {
    String normalized = PhoneUtils.normalize(request.phone());
    if (userRepository.existsByPhone(normalized)) {
      throw new UserAlreadyExistsException("Phone already registered");
    }
    String tempPin = generatePin();
    User user = User.builder()
        .phone(normalized)
        .fullName(request.fullName())
        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
        .pinHash(passwordEncoder.encode(tempPin))
        .role(UserRole.AGENT)
        .status(UserStatus.ACTIVE)
        .pinAttempts((short) 0)
        .kycLevel((short) 0)
        .build();
    userRepository.save(user);
    walletRepository.save(Wallet.builder()
        .user(user)
        .balanceKc(0)
        .status(WalletStatus.ACTIVE)
        .build());
    Agent agent = Agent.builder()
        .user(user)
        .businessName(request.businessName())
        .location(request.location())
        .floatLimitKc(request.floatLimitKc())
        .floatBalanceKc(0)
        .cashHeldUsd(java.math.BigDecimal.ZERO)
        .status(com.koini.core.domain.enums.AgentStatus.ACTIVE)
        .build();
    agentRepository.save(agent);
    smsService.sendGenericAlert(user.getPhone(), "Your temporary PIN is " + tempPin);
    auditService.log("AGENT_REGISTERED", user.getUserId(), "ADMIN",
        "User", user.getUserId().toString(), null, user, AuditOutcome.SUCCESS, httpRequest);
    return new RegisterResponse(user.getUserId().toString(), PhoneUtils.mask(user.getPhone()),
        "Agent registered");
  }

  /**
   * Authenticates a user and returns access/refresh tokens.
   */
  @Transactional
  public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
    String identifier = request.phone();
    boolean isPhone = PhoneUtils.isValid(identifier);
    String rateKey = "koini:rate:login:" + (isPhone ? PhoneUtils.normalize(identifier) : identifier.toLowerCase());
    boolean allowed = rateLimitService.checkLimit(rateKey, KoiniConstants.LOGIN_MAX_ATTEMPTS,
        Duration.ofMinutes(KoiniConstants.LOGIN_WINDOW_MINUTES));
    if (!allowed) {
      throw new AuthenticationException("Too many login attempts. Please try again later.");
    }
    User user;
    if (isPhone) {
      String normalized = PhoneUtils.normalize(identifier);
      user = userRepository.findByPhone(normalized)
          .or(() -> normalized.equals(identifier) ? java.util.Optional.empty()
              : userRepository.findByPhone(identifier))
          .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    } else {
      user = userRepository.findByFullNameIgnoreCaseAndRole(identifier, UserRole.ADMIN)
          .orElseThrow(() -> new AuthenticationException("Invalid credentials"));
    }
    if (user.getStatus() == UserStatus.SUSPENDED) {
      throw new AccountSuspendedException("Account suspended");
    }
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new AuthenticationException("Invalid credentials");
    }
    UserSummaryResponse summary = userMapper.toSummary(user);
    String accessToken = jwtService.generateAccessToken(
        new KoiniUserDetails(user), Map.of("role", user.getRole().canonical().name()));
    String refreshTokenValue = jwtService.generateRefreshToken();
    RefreshToken refreshToken = RefreshToken.builder()
        .user(user)
        .tokenHash(TokenUtils.sha256(refreshTokenValue))
        .expiresAt(LocalDateTime.now().plusDays(7))
        .revoked(false)
        .build();
    refreshTokenRepository.save(refreshToken);
    user.setLastLogin(LocalDateTime.now());
    auditService.log("LOGIN_SUCCESS", user.getUserId(), user.getRole().name(),
        "User", user.getUserId().toString(), null, user, AuditOutcome.SUCCESS, httpRequest);
    return new LoginResponse(accessToken, refreshTokenValue, 900_000L, summary);
  }

  /**
   * Refreshes access and refresh tokens using rotation.
   */
  @Transactional
  public LoginResponse refreshToken(RefreshTokenRequest request, HttpServletRequest httpRequest) {
    String hash = TokenUtils.sha256(request.refreshToken());
    RefreshToken token = refreshTokenRepository.findByTokenHash(hash)
        .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));
    if (token.isRevoked() || token.getExpiresAt().isBefore(LocalDateTime.now())) {
      refreshTokenRepository.deleteByUserUserId(token.getUser().getUserId());
      throw new AuthenticationException("Refresh token expired");
    }
    token.setRevoked(true);
    refreshTokenRepository.save(token);

    User user = token.getUser();
    String accessToken = jwtService.generateAccessToken(
        new KoiniUserDetails(user), Map.of("role", user.getRole().canonical().name()));
    String refreshTokenValue = jwtService.generateRefreshToken();
    RefreshToken newToken = RefreshToken.builder()
        .user(user)
        .tokenHash(TokenUtils.sha256(refreshTokenValue))
        .expiresAt(LocalDateTime.now().plusDays(7))
        .revoked(false)
        .build();
    refreshTokenRepository.save(newToken);
    UserSummaryResponse summary = userMapper.toSummary(user);
    return new LoginResponse(accessToken, refreshTokenValue, 900_000L, summary);
  }

  /**
   * Revokes a refresh token for logout.
   */
  @Transactional
  @PreAuthorize("isAuthenticated()")
  public void logout(String refreshToken, UUID userId) {
    String hash = TokenUtils.sha256(refreshToken);
    refreshTokenRepository.findByTokenHash(hash)
        .filter(token -> token.getUser().getUserId().equals(userId))
        .ifPresent(token -> {
          token.setRevoked(true);
          refreshTokenRepository.save(token);
        });
  }

  /**
   * Changes a user's PIN after verification.
   */
  @Transactional
  @PreAuthorize("isAuthenticated()")
  public void changePin(ChangePinRequest request, UUID userId, HttpServletRequest httpRequest) {
    if (!request.newPin().equals(request.confirmNewPin())) {
      throw new AuthenticationException("PIN confirmation does not match");
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    if (user.getPinHash() == null || !passwordEncoder.matches(request.currentPin(), user.getPinHash())) {
      throw new AuthenticationException("Invalid current PIN");
    }
    user.setPinHash(passwordEncoder.encode(request.newPin()));
    refreshTokenRepository.deleteByUserUserId(userId);
    smsService.sendGenericAlert(user.getPhone(), "Your PIN has been changed.");
    auditService.log("PIN_CHANGED", userId, user.getRole().name(),
        "User", userId.toString(), null, null, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Sets up a PIN for the first time.
   */
  @Transactional
  @PreAuthorize("isAuthenticated()")
  public void setupPin(SetupPinRequest request, UUID userId) {
    if (!request.pin().equals(request.confirmPin())) {
      throw new AuthenticationException("PIN confirmation does not match");
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    user.setPinHash(passwordEncoder.encode(request.pin()));
  }

  /**
   * Verifies PIN for sensitive operations.
   */
  @Transactional
  @PreAuthorize("permitAll()")
  public void verifyPin(String pin, UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    if (user.getPinHash() == null) {
      throw new PinRequiredException("PIN not set");
    }
    if (user.getPinLockedUntil() != null && user.getPinLockedUntil().isAfter(LocalDateTime.now())) {
      throw new AccountLockedException("PIN locked. Try again later.");
    }
    if (!passwordEncoder.matches(pin, user.getPinHash())) {
      short attempts = (short) (user.getPinAttempts() + 1);
      user.setPinAttempts(attempts);
      if (attempts >= KoiniConstants.PIN_MAX_ATTEMPTS) {
        user.setPinLockedUntil(LocalDateTime.now().plusMinutes(KoiniConstants.PIN_LOCK_MINUTES));
        smsService.sendPinLockAlert(user.getPhone(), KoiniConstants.PIN_LOCK_MINUTES);
      }
      throw new AuthenticationException("Invalid PIN");
    }
    user.setPinAttempts((short) 0);
    user.setPinLockedUntil(null);
  }

  private String generatePin() {
    Random random = new java.security.SecureRandom();
    int value = random.nextInt(9000) + 1000;
    return String.valueOf(value);
  }
}
