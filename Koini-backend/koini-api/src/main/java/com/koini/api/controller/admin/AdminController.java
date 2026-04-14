package com.koini.api.controller.admin;

import com.koini.api.dto.request.CreateRouteRequest;
import com.koini.api.dto.request.RegisterAgentRequest;
import com.koini.api.dto.request.RegisterConductorRequest;
import com.koini.api.dto.request.RegisterMerchantRequest;
import com.koini.api.dto.request.SuspendUserRequest;
import com.koini.api.dto.request.UpdateRouteRequest;
import com.koini.api.dto.response.AdminUserDetailResponse;
import com.koini.api.dto.response.AgentSummaryResponse;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.AuditLogResponse;
import com.koini.api.dto.response.DashboardResponse;
import com.koini.api.dto.response.ReconciliationResponse;
import com.koini.api.dto.response.RouteResponse;
import com.koini.api.dto.response.TransactionResponse;
import com.koini.api.dto.response.UserSummaryResponse;
import com.koini.api.dto.response.MerchantKycApplicationResponse;
import com.koini.api.service.admin.AdminService;
import com.koini.api.service.admin.MerchantKycAdminService;
import com.koini.api.service.auth.AuthService;
import com.koini.api.service.reconciliation.ReconciliationService;
import com.koini.api.util.SecurityUtils;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.persistence.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

  private final AdminService adminService;
  private final MerchantKycAdminService merchantKycAdminService;
  private final AuthService authService;
  private final ReconciliationService reconciliationService;
  private final AuditLogRepository auditLogRepository;

  public AdminController(
      AdminService adminService,
      MerchantKycAdminService merchantKycAdminService,
      AuthService authService,
      ReconciliationService reconciliationService,
      AuditLogRepository auditLogRepository
  ) {
    this.adminService = adminService;
    this.merchantKycAdminService = merchantKycAdminService;
    this.authService = authService;
    this.reconciliationService = reconciliationService;
    this.auditLogRepository = auditLogRepository;
  }

  @Operation(summary = "Dashboard", description = "Returns system stats overview")
  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.ok(ApiResponse.success(adminService.getDashboardStats(), requestId));
  }

  @Operation(summary = "Users", description = "List users")
  @GetMapping("/users")
  public ResponseEntity<ApiResponse<Page<UserSummaryResponse>>> users(
      @RequestParam(name = "role", required = false) String role,
      @RequestParam(name = "status", required = false) String status,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    Pageable pageable = PageRequest.of(page, size);
    UserRole roleEnum = parseRole(role);
    UserStatus statusEnum = parseStatus(status);
    Page<UserSummaryResponse> response = adminService.getAllUsers(roleEnum, statusEnum, pageable);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Pending merchants", description = "Lists merchant accounts pending verification")
  @GetMapping("/merchants/pending")
  public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> pendingMerchants(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.ok(ApiResponse.success(adminService.listPendingMerchants(), requestId));
  }

  @Operation(summary = "Merchant application", description = "Returns merchant onboarding/KYC application for review")
  @GetMapping("/merchants/{userId}/application")
  public ResponseEntity<ApiResponse<MerchantKycApplicationResponse>> merchantApplication(
      @PathVariable("userId") String userId,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    MerchantKycApplicationResponse response = merchantKycAdminService.getApplication(UUID.fromString(userId));
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Merchant document", description = "Downloads merchant onboarding document (admin-only)")
  @GetMapping("/merchants/{userId}/documents/{type}")
  public ResponseEntity<Resource> merchantDocument(
      @PathVariable("userId") String userId,
      @PathVariable("type") String type) {
    MerchantKycAdminService.MerchantDocument document =
        merchantKycAdminService.getDocument(UUID.fromString(userId), type);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.filename() + "\"")
        .contentType(document.contentType() != null ? document.contentType() : MediaType.APPLICATION_OCTET_STREAM)
        .body(document.resource());
  }

  @Operation(summary = "Approve merchant", description = "Approves a merchant account")
  @PostMapping("/merchants/{userId}/approve")
  public ResponseEntity<ApiResponse<?>> approveMerchant(
      @PathVariable("userId") String userId,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    adminService.approveMerchant(UUID.fromString(userId), SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success("approved", requestId));
  }

  @Operation(summary = "Reject merchant", description = "Rejects a merchant account")
  @PostMapping("/merchants/{userId}/reject")
  public ResponseEntity<ApiResponse<?>> rejectMerchant(
      @PathVariable("userId") String userId,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    adminService.rejectMerchant(UUID.fromString(userId), SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success("rejected", requestId));
  }

  @Operation(summary = "User detail", description = "Get user detail")
  @GetMapping("/users/{id}")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> userDetail(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    AdminUserDetailResponse response = adminService.getUserDetail(UUID.fromString(id));
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Register merchant", description = "Registers a merchant")
  @PostMapping("/users/merchant")
  public ResponseEntity<ApiResponse<?>> registerMerchant(
      @Valid @RequestBody RegisterMerchantRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.status(201).body(ApiResponse.success(
        authService.registerMerchant(request, httpRequest), requestId));
  }

  @Hidden
  @Operation(summary = "Register conductor (legacy)", description = "Legacy alias for merchant registration")
  @PostMapping("/users/conductor")
  public ResponseEntity<ApiResponse<?>> registerConductor(
      @Valid @RequestBody RegisterConductorRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.status(201).body(ApiResponse.success(
        authService.registerConductor(request, httpRequest), requestId));
  }

  @Operation(summary = "Register agent", description = "Registers an agent")
  @PostMapping("/users/agent")
  public ResponseEntity<ApiResponse<?>> registerAgent(
      @Valid @RequestBody RegisterAgentRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.status(201).body(ApiResponse.success(
        authService.registerAgent(request, httpRequest), requestId));
  }

  @Operation(summary = "Suspend user", description = "Suspends a user")
  @PatchMapping("/users/{id}/suspend")
  public ResponseEntity<ApiResponse<?>> suspendUser(
      @PathVariable("id") String id,
      @Valid @RequestBody SuspendUserRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    adminService.suspendUser(UUID.fromString(id), request.reason(), SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success("suspended", requestId));
  }

  @Operation(summary = "Activate user", description = "Activates a user")
  @PatchMapping("/users/{id}/activate")
  public ResponseEntity<ApiResponse<?>> activateUser(
      @PathVariable("id") String id,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    adminService.activateUser(UUID.fromString(id), SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success("activated", requestId));
  }

  @Operation(summary = "Activate user (legacy POST)", description = "Legacy alias for activate user")
  @PostMapping("/users/{id}/activate")
  public ResponseEntity<ApiResponse<?>> activateUserPost(
      @PathVariable("id") String id,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return activateUser(id, httpRequest, requestId);
  }

  @Operation(summary = "Transactions", description = "List all transactions")
  @GetMapping("/transactions")
  public ResponseEntity<ApiResponse<Page<TransactionResponse>>> transactions(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    Pageable pageable = PageRequest.of(page, size);
    return ResponseEntity.ok(ApiResponse.success(adminService.getAllTransactions(pageable), requestId));
  }

  @Operation(summary = "Audit logs", description = "List audit logs")
  @GetMapping("/audit-logs")
  public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> auditLogs(
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "20") int size,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    Pageable pageable = PageRequest.of(page, size);
    Page<AuditLogResponse> response = auditLogRepository.findAll(pageable).map(log ->
        new AuditLogResponse(log.getLogId().toString(), log.getAction(), log.getEntityType(),
            log.getEntityId(), log.getActorId() != null ? log.getActorId().toString() : null,
            log.getOutcome() != null ? log.getOutcome().name() : null,
            log.getCreatedAt() != null ? log.getCreatedAt().toString() : null));
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Create route", description = "Creates route")
  @PostMapping("/routes")
  public ResponseEntity<ApiResponse<RouteResponse>> createRoute(
      @Valid @RequestBody CreateRouteRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    RouteResponse response = adminService.createRoute(request, SecurityUtils.currentUserId());
    return ResponseEntity.status(201).body(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "List routes", description = "Lists routes")
  @GetMapping("/routes")
  public ResponseEntity<ApiResponse<List<RouteResponse>>> listRoutes(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.ok(ApiResponse.success(adminService.getAllRoutes(), requestId));
  }

  @Operation(summary = "Update route", description = "Updates route")
  @PutMapping("/routes/{id}")
  public ResponseEntity<ApiResponse<RouteResponse>> updateRoute(
      @PathVariable("id") String id,
      @Valid @RequestBody UpdateRouteRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    RouteResponse response = adminService.updateRoute(UUID.fromString(id), request);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Deactivate route", description = "Deactivates route")
  @PatchMapping("/routes/{id}/deactivate")
  public ResponseEntity<ApiResponse<?>> deactivateRoute(
      @PathVariable("id") String id,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    adminService.deactivateRoute(UUID.fromString(id));
    return ResponseEntity.ok(ApiResponse.success("deactivated", requestId));
  }

  @Operation(summary = "Reconciliation", description = "Returns reconciliation report")
  @GetMapping("/reconciliation")
  public ResponseEntity<ApiResponse<ReconciliationResponse>> reconciliation(
      @RequestParam(name = "date", required = false) String date,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    LocalDate target = (date != null && !date.isBlank()) ? LocalDate.parse(date) : LocalDate.now();
    return ResponseEntity.ok(ApiResponse.success(reconciliationService.getReport(target), requestId));
  }

  @Operation(summary = "Agents", description = "List agents")
  @GetMapping("/agents")
  public ResponseEntity<ApiResponse<List<AgentSummaryResponse>>> agents(
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    return ResponseEntity.ok(ApiResponse.success(adminService.getAgents(), requestId));
  }

  private UserRole parseRole(String role) {
    if (role == null || role.isBlank()) {
      return null;
    }
    try {
      return UserRole.valueOf(role.trim().toUpperCase()).canonical();
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private UserStatus parseStatus(String status) {
    if (status == null || status.isBlank()) {
      return null;
    }
    try {
      return UserStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
