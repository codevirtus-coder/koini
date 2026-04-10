package com.koini.api.controller.auth;

import com.koini.api.dto.request.ChangePinRequest;
import com.koini.api.dto.request.LoginRequest;
import com.koini.api.dto.request.RefreshTokenRequest;
import com.koini.api.dto.request.RegisterMerchantSelfRequest;
import com.koini.api.dto.request.RegisterPassengerRequest;
import com.koini.api.dto.request.SetupPinRequest;
import com.koini.api.dto.response.ApiResponse;
import com.koini.api.dto.response.LoginResponse;
import com.koini.api.dto.response.RegisterResponse;
import com.koini.api.service.auth.AuthService;
import com.koini.api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @Operation(summary = "Register passenger", description = "Registers a new passenger account")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registered"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
  })
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResponse>> register(
      @Valid @RequestBody RegisterPassengerRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    RegisterResponse response = authService.registerPassenger(request, httpRequest);
    return ResponseEntity.status(201).body(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Register merchant", description = "Registers a new merchant account (self-service)")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registered"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request")
  })
  @PostMapping("/register/merchant")
  public ResponseEntity<ApiResponse<RegisterResponse>> registerMerchant(
      @Valid @RequestBody RegisterMerchantSelfRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    RegisterResponse response = authService.registerMerchantSelf(request, httpRequest);
    return ResponseEntity.status(201).body(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Login", description = "Authenticates a user and returns tokens")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    LoginResponse response = authService.login(request, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Refresh token", description = "Refreshes access token")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResponse>> refresh(
      @Valid @RequestBody RefreshTokenRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    LoginResponse response = authService.refreshToken(request, httpRequest);
    return ResponseEntity.ok(ApiResponse.success(response, requestId));
  }

  @Operation(summary = "Logout", description = "Revokes refresh token")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logged out")
  })
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Map<String, String>>> logout(
      @Valid @RequestBody RefreshTokenRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    authService.logout(request.refreshToken(), SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "logged_out"), requestId));
  }

  @Operation(summary = "Change PIN", description = "Changes user PIN")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PIN changed"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error")
  })
  @PutMapping("/pin/change")
  public ResponseEntity<ApiResponse<Map<String, String>>> changePin(
      @Valid @RequestBody ChangePinRequest request,
      HttpServletRequest httpRequest,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    authService.changePin(request, SecurityUtils.currentUserId(), httpRequest);
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "pin_changed"), requestId));
  }

  @Operation(summary = "Setup PIN", description = "Sets up PIN for first time")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PIN set")
  })
  @PostMapping("/pin/setup")
  public ResponseEntity<ApiResponse<Map<String, String>>> setupPin(
      @Valid @RequestBody SetupPinRequest request,
      @RequestHeader(value = "X-Request-ID", required = false) String requestId) {
    authService.setupPin(request, SecurityUtils.currentUserId());
    return ResponseEntity.ok(ApiResponse.success(Map.of("status", "pin_set"), requestId));
  }
}
