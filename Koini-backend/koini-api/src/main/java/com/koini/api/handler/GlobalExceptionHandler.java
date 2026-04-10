package com.koini.api.handler;

import com.koini.core.exception.AccessDeniedException;
import com.koini.core.exception.AccountLockedException;
import com.koini.core.exception.AccountSuspendedException;
import com.koini.core.exception.AuthenticationException;
import com.koini.core.exception.DuplicatePaymentException;
import com.koini.core.exception.InsufficientBalanceException;
import com.koini.core.exception.MisconfigurationException;
import com.koini.core.exception.MerchantNotApprovedException;
import com.koini.core.exception.PaymentCodeAlreadyUsedException;
import com.koini.core.exception.PaymentCodeExpiredException;
import com.koini.core.exception.PaymentCodeInvalidException;
import com.koini.core.exception.PaymentRequestExpiredException;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.core.exception.TokenExpiredException;
import com.koini.core.exception.TokenInvalidException;
import com.koini.core.exception.UserNotFoundException;
import com.koini.core.exception.UserAlreadyExistsException;
import com.koini.core.exception.WalletFrozenException;
import com.koini.core.exception.WalletNotFoundException;
import com.koini.core.exception.PinRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleUserAlreadyExists(UserAlreadyExistsException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(TokenExpiredException.class)
  public ResponseEntity<ErrorResponse> handleTokenExpired(TokenExpiredException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(TokenInvalidException.class)
  public ResponseEntity<ErrorResponse> handleTokenInvalid(TokenInvalidException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(AccountLockedException.class)
  public ResponseEntity<ErrorResponse> handleLocked(AccountLockedException ex, HttpServletRequest request) {
    return build(HttpStatus.LOCKED, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(AccountSuspendedException.class)
  public ResponseEntity<ErrorResponse> handleSuspended(AccountSuspendedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(InsufficientBalanceException.class)
  public ResponseEntity<ErrorResponse> handleBalance(InsufficientBalanceException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(WalletFrozenException.class)
  public ResponseEntity<ErrorResponse> handleWalletFrozen(WalletFrozenException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(PaymentCodeExpiredException.class)
  public ResponseEntity<ErrorResponse> handleCodeExpired(PaymentCodeExpiredException ex, HttpServletRequest request) {
    return build(HttpStatus.GONE, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(PaymentCodeAlreadyUsedException.class)
  public ResponseEntity<ErrorResponse> handleCodeUsed(PaymentCodeAlreadyUsedException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(PaymentCodeInvalidException.class)
  public ResponseEntity<ErrorResponse> handleCodeInvalid(PaymentCodeInvalidException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(PaymentRequestExpiredException.class)
  public ResponseEntity<ErrorResponse> handleRequestExpired(PaymentRequestExpiredException ex,
      HttpServletRequest request) {
    return build(HttpStatus.GONE, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(DuplicatePaymentException.class)
  public ResponseEntity<ErrorResponse> handleDuplicate(DuplicatePaymentException ex, HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(WalletNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleWalletNotFound(WalletNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.NOT_FOUND, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(PinRequiredException.class)
  public ResponseEntity<ErrorResponse> handlePinRequired(PinRequiredException ex, HttpServletRequest request) {
    return build(HttpStatus.PRECONDITION_REQUIRED, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(MerchantNotApprovedException.class)
  public ResponseEntity<ErrorResponse> handleMerchantNotApproved(MerchantNotApprovedException ex,
      HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, ex.getErrorCode(), ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleSpringAccessDenied(
      org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
    return build(HttpStatus.FORBIDDEN, "AUTH_403", "Access denied", request, null, false);
  }

  @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleAuthMissing(
      AuthenticationCredentialsNotFoundException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "AUTH_401", "Authentication required", request, null, false);
  }

  @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleSpringAuth(
      org.springframework.security.core.AuthenticationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNAUTHORIZED, "AUTH_401", "Authentication required", request, null, false);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> details = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return build(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_001",
        "Validation failed", request, details, false);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "REQ_001", "Malformed JSON request", request, null, false);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex,
      HttpServletRequest request) {
    return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ_002", "Unsupported content type", request, null, false);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
      HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "REQ_003", ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "REQ_004", ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
      HttpServletRequest request) {
    return build(HttpStatus.BAD_REQUEST, "REQ_005", ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, "VALIDATION_002", ex.getMessage(), request, null, false);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
      HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "DATA_001", "Data integrity violation", request, null, false);
  }

  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimistic(OptimisticLockingFailureException ex,
      HttpServletRequest request) {
    return build(HttpStatus.CONFLICT, "SYS_001", "Concurrent modification. Please retry.", request, null, false);
  }

  @ExceptionHandler(MisconfigurationException.class)
  public ResponseEntity<ErrorResponse> handleMisconfiguration(MisconfigurationException ex, HttpServletRequest request) {
    return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getErrorCode(), ex.getMessage(), request, ex.getDetails(), false);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
    log.error("Unhandled exception for {} {}",
        request != null ? request.getMethod() : "?",
        request != null ? request.getRequestURI() : "?", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "SYS_999", "Unexpected error", request, null, true);
  }

  private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message,
      HttpServletRequest request, Object details, boolean isError) {
    String requestId = request.getHeader("X-Request-ID");
    if (isError) {
      log.error("{} {} - {}", status.value(), request.getRequestURI(), message, request);
    } else {
      log.warn("{} {} - {}", status.value(), request.getRequestURI(), message);
    }
    ErrorResponse response = ErrorResponse.of(code, message, requestId, request.getRequestURI(), details);
    return ResponseEntity.status(status).body(response);
  }
}
