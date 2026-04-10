package com.koini.api.handler;

import java.time.Instant;

public record ErrorResponse(
    boolean success,
    String errorCode,
    String message,
    String timestamp,
    String requestId,
    String path,
    Object details
) {
  public static ErrorResponse of(String errorCode, String message, String requestId, String path, Object details) {
    return new ErrorResponse(false, errorCode, message, Instant.now().toString(), requestId, path, details);
  }
}
