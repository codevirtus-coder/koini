package com.koini.api.service.admin;

import com.koini.api.dto.request.CreateRouteRequest;
import com.koini.api.dto.request.UpdateRouteRequest;
import com.koini.api.dto.response.AdminUserDetailResponse;
import com.koini.api.dto.response.AgentSummaryResponse;
import com.koini.api.dto.response.DashboardResponse;
import com.koini.api.dto.response.RouteResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.dto.response.UserSummaryResponse;
import com.koini.api.mapper.TransactionMapper;
import com.koini.api.mapper.UserMapper;
import com.koini.api.service.AuditService;
import com.koini.core.domain.entity.Agent;
import com.koini.core.domain.entity.Route;
import com.koini.core.domain.entity.Transaction;
import com.koini.core.domain.entity.User;
import com.koini.core.domain.entity.Wallet;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.core.domain.enums.PaymentCodeStatus;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.domain.enums.WalletStatus;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.AuditLogRepository;
import com.koini.persistence.repository.PaymentCodeRepository;
import com.koini.persistence.repository.RefreshTokenRepository;
import com.koini.persistence.repository.RouteRepository;
import com.koini.persistence.repository.TransactionRepository;
import com.koini.persistence.repository.UserRepository;
import com.koini.persistence.repository.WalletRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

  private final UserRepository userRepository;
  private final WalletRepository walletRepository;
  private final TransactionRepository transactionRepository;
  private final PaymentCodeRepository paymentCodeRepository;
  private final AgentRepository agentRepository;
  private final RouteRepository routeRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final AuditLogRepository auditLogRepository;
  private final UserMapper userMapper;
  private final TransactionMapper transactionMapper;
  private final AuditService auditService;

  public AdminService(
      UserRepository userRepository,
      WalletRepository walletRepository,
      TransactionRepository transactionRepository,
      PaymentCodeRepository paymentCodeRepository,
      AgentRepository agentRepository,
      RouteRepository routeRepository,
      RefreshTokenRepository refreshTokenRepository,
      AuditLogRepository auditLogRepository,
      UserMapper userMapper,
      TransactionMapper transactionMapper,
      AuditService auditService
  ) {
    this.userRepository = userRepository;
    this.walletRepository = walletRepository;
    this.transactionRepository = transactionRepository;
    this.paymentCodeRepository = paymentCodeRepository;
    this.agentRepository = agentRepository;
    this.routeRepository = routeRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.auditLogRepository = auditLogRepository;
    this.userMapper = userMapper;
    this.transactionMapper = transactionMapper;
    this.auditService = auditService;
  }

  /**
   * Returns admin dashboard statistics.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public DashboardResponse getDashboardStats() {
    long passengers = userRepository.countByRole(UserRole.CLIENT) + userRepository.countByRole(UserRole.PASSENGER);
    long conductors = userRepository.countByRole(UserRole.MERCHANT) + userRepository.countByRole(UserRole.CONDUCTOR);
    long agents = userRepository.countByRole(UserRole.AGENT);
    long totalBalance = walletRepository.sumBalances();
    long transactionsToday = transactionRepository.sumByTypeToday(com.koini.core.domain.enums.TransactionType.TOPUP);
    long volumeToday = transactionRepository.sumByTypeToday(com.koini.core.domain.enums.TransactionType.FARE_PAYMENT);
    long activeCodes = paymentCodeRepository.countByStatus(PaymentCodeStatus.PENDING);
    long flagged = userRepository.countByStatus(UserStatus.SUSPENDED) + userRepository.countByStatus(UserStatus.LOCKED);
    return new DashboardResponse(passengers, conductors, agents, totalBalance,
        MoneyUtils.formatUsd(totalBalance), transactionsToday, volumeToday, activeCodes, flagged);
  }

  /**
   * Lists users with optional role and status filters.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public Page<UserSummaryResponse> getAllUsers(UserRole role, UserStatus status, Pageable pageable) {
    Page<User> users;
    if (role != null && status != null) {
      users = userRepository.findByRoleAndStatus(role, status, pageable);
    } else {
      users = userRepository.findAll(pageable);
    }
    return users.map(userMapper::toSummary);
  }

  /**
   * Lists merchant accounts pending verification (approval required).
   */
  @PreAuthorize("hasRole('ADMIN')")
  public List<UserSummaryResponse> listPendingMerchants() {
    List<User> users = userRepository.findByRoleInAndStatus(
        List.of(UserRole.MERCHANT, UserRole.CONDUCTOR), UserStatus.PENDING_VERIFICATION);
    return users.stream().map(userMapper::toSummary).toList();
  }

  /**
   * Approves a merchant: sets status ACTIVE and unfreezes wallet.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void approveMerchant(UUID merchantUserId, UUID adminId, HttpServletRequest httpRequest) {
    User user = userRepository.findById(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() == null || user.getRole().canonical() != UserRole.MERCHANT) {
      throw new ResourceNotFoundException("Merchant not found");
    }
    user.setStatus(UserStatus.ACTIVE);
    walletRepository.findByUserUserId(merchantUserId).ifPresent(wallet -> wallet.setStatus(WalletStatus.ACTIVE));
    auditService.log("MERCHANT_APPROVED", adminId, "ADMIN", "User", merchantUserId.toString(),
        null, null, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Rejects a merchant: sets status LOCKED and freezes wallet.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void rejectMerchant(UUID merchantUserId, UUID adminId, HttpServletRequest httpRequest) {
    User user = userRepository.findById(merchantUserId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() == null || user.getRole().canonical() != UserRole.MERCHANT) {
      throw new ResourceNotFoundException("Merchant not found");
    }
    user.setStatus(UserStatus.LOCKED);
    walletRepository.findByUserUserId(merchantUserId).ifPresent(wallet -> wallet.setStatus(WalletStatus.FROZEN));
    refreshTokenRepository.deleteByUserUserId(merchantUserId);
    auditService.log("MERCHANT_REJECTED", adminId, "ADMIN", "User", merchantUserId.toString(),
        null, null, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Returns full user detail for admin.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public AdminUserDetailResponse getUserDetail(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    Wallet wallet = walletRepository.findByUserUserId(userId).orElse(null);
    AdminUserDetailResponse response = userMapper.toAdminDetail(user, wallet);
    if (wallet != null) {
      Page<Transaction> txs = transactionRepository.findByFromWalletOrToWalletOrderByCreatedAtDesc(
          wallet, wallet, PageRequest.of(0, 10));
      List<AdminUserDetailResponse.TransactionSummary> summaries = txs.getContent().stream()
          .map(tx -> new AdminUserDetailResponse.TransactionSummary(
              tx.getTxId().toString(),
              tx.getTxType().name(),
              tx.getAmountKc(),
              MoneyUtils.formatUsd(tx.getAmountKc()),
              tx.getStatus().name(),
              tx.getCreatedAt() != null ? tx.getCreatedAt().toString() : null))
          .toList();
      response.setRecentTransactions(summaries);
    }
    agentRepository.findByUserUserId(userId).ifPresent(agent -> response.setAgentDetail(
        new AdminUserDetailResponse.AgentDetail(agent.getAgentId().toString(), agent.getBusinessName(),
            agent.getFloatBalanceKc(), agent.getStatus().name())));
    return response;
  }

  /**
   * Suspends a user and freezes their wallet.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void suspendUser(UUID userId, String reason, UUID adminId, HttpServletRequest httpRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setStatus(UserStatus.SUSPENDED);
    walletRepository.findByUserUserId(userId).ifPresent(wallet -> wallet.setStatus(WalletStatus.FROZEN));
    refreshTokenRepository.deleteByUserUserId(userId);
    auditService.log("USER_SUSPENDED", adminId, "ADMIN", "User", userId.toString(),
        null, reason, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Activates a user and unfreezes their wallet.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void activateUser(UUID userId, UUID adminId, HttpServletRequest httpRequest) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    user.setStatus(UserStatus.ACTIVE);
    walletRepository.findByUserUserId(userId).ifPresent(wallet -> wallet.setStatus(WalletStatus.ACTIVE));
    auditService.log("USER_ACTIVATED", adminId, "ADMIN", "User", userId.toString(),
        null, null, AuditOutcome.SUCCESS, httpRequest);
  }

  /**
   * Creates a route.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public RouteResponse createRoute(CreateRouteRequest request, UUID adminId) {
    Route route = Route.builder()
        .name(request.name())
        .origin(request.origin())
        .destination(request.destination())
        .fareKc(request.fareKc())
        .isActive(true)
        .createdBy(User.builder().userId(adminId).build())
        .build();
    routeRepository.save(route);
    return toRouteResponse(route);
  }

  /**
   * Updates a route.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public RouteResponse updateRoute(UUID routeId, UpdateRouteRequest request) {
    Route route = routeRepository.findById(routeId)
        .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    if (request.name() != null) {
      route.setName(request.name());
    }
    if (request.origin() != null) {
      route.setOrigin(request.origin());
    }
    if (request.destination() != null) {
      route.setDestination(request.destination());
    }
    if (request.fareKc() > 0) {
      route.setFareKc(request.fareKc());
    }
    if (request.isActive() != null) {
      route.setActive(request.isActive());
    }
    return toRouteResponse(route);
  }

  /**
   * Deactivates a route.
   */
  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void deactivateRoute(UUID routeId) {
    Route route = routeRepository.findById(routeId)
        .orElseThrow(() -> new ResourceNotFoundException("Route not found"));
    route.setActive(false);
  }

  /**
   * Returns all routes.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public List<RouteResponse> getAllRoutes() {
    return routeRepository.findAll().stream().map(this::toRouteResponse).toList();
  }

  /**
   * Returns all transactions for admin.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
    return transactionRepository.findAll(pageable).map(transactionMapper::toResponse);
  }

  /**
   * Returns all agents with float balances.
   */
  @PreAuthorize("hasRole('ADMIN')")
  public List<AgentSummaryResponse> getAgents() {
    return agentRepository.findAll().stream()
        .map(agent -> new AgentSummaryResponse(
            agent.getAgentId().toString(),
            agent.getUser().getUserId().toString(),
            agent.getBusinessName(),
            agent.getFloatBalanceKc(),
            MoneyUtils.formatUsd(agent.getFloatBalanceKc()),
            agent.getStatus().name()))
        .toList();
  }

  private RouteResponse toRouteResponse(Route route) {
    return new RouteResponse(
        route.getRouteId().toString(),
        route.getName(),
        route.getOrigin(),
        route.getDestination(),
        route.getFareKc(),
        MoneyUtils.formatUsd(route.getFareKc()),
        route.isActive());
  }
}
