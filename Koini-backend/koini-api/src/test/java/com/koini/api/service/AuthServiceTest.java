package com.koini.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.koini.api.dto.request.RegisterPassengerRequest;
import com.koini.api.dto.request.RegisterMerchantSelfRequest;
import com.koini.api.dto.response.RegisterResponse;
import com.koini.api.mapper.UserMapper;
import com.koini.api.service.auth.AuthService;
import com.koini.api.service.auth.RateLimitService;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.notification.sms.SmsService;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.RefreshTokenRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import com.koini.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock WalletRepository walletRepository;
  @Mock AgentRepository agentRepository;
  @Mock RefreshTokenRepository refreshTokenRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtService jwtService;
  @Mock RateLimitService rateLimitService;
  @Mock SmsService smsService;
  @Mock AuditService auditService;
  @Mock UserMapper userMapper;
  @Mock HttpServletRequest httpServletRequest;

  private AuthService authService;

  @BeforeEach
  void setup() {
    authService = new AuthService(userRepository, walletRepository, agentRepository,
        refreshTokenRepository, passwordEncoder, jwtService, rateLimitService, smsService,
        auditService, userMapper);
  }

  @Test
  @DisplayName("registerPassenger: should create user and wallet")
  void registerPassenger_success() {
    when(userRepository.existsByPhone(any())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("hash");
    when(userRepository.save(any())).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setUserId(java.util.UUID.randomUUID());
      return user;
    });

    RegisterPassengerRequest request = new RegisterPassengerRequest("0771234567", "password123", "Test");
    RegisterResponse response = authService.registerPassenger(request, httpServletRequest);

    assertThat(response.userId()).isNotBlank();
    assertThat(response.maskedPhone()).contains("****");
  }

  @Test
  @DisplayName("registerMerchantSelf: should create merchant with PENDING_VERIFICATION")
  void registerMerchantSelf_success() {
    when(userRepository.existsByPhone(any())).thenReturn(false);
    when(passwordEncoder.encode(any())).thenReturn("hash");
    when(userRepository.save(any())).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setUserId(java.util.UUID.randomUUID());
      return user;
    });

    RegisterMerchantSelfRequest request = new RegisterMerchantSelfRequest("0771234567", "password123", "Merchant");
    RegisterResponse response = authService.registerMerchantSelf(request, httpServletRequest);

    assertThat(response.userId()).isNotBlank();
    assertThat(response.maskedPhone()).contains("****");

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    assertThat(userCaptor.getValue().getRole()).isEqualTo(UserRole.MERCHANT);
    assertThat(userCaptor.getValue().getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
  }
}
