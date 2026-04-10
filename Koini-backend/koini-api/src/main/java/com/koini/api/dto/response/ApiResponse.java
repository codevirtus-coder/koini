package com.koini.api.dto.response;

import java.time.Instant;

public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    String requestId,
    String timestamp
) {
  public static <T> ApiResponse<T> success(T data, String message, String requestId) {
    return new ApiResponse<>(true, message, data, requestId, Instant.now().toString());
  }

  public static <T> ApiResponse<T> success(T data, String requestId) {
    return new ApiResponse<>(true, "success", data, requestId, Instant.now().toString());
  }
}
