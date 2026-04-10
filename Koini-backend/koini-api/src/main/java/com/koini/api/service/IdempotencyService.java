package com.koini.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

  private final RedisService redisService;
  private final ObjectMapper objectMapper;

  public IdempotencyService(RedisService redisService, ObjectMapper objectMapper) {
    this.redisService = redisService;
    this.objectMapper = objectMapper;
  }

  public <T> Optional<T> get(String key, Class<T> type) {
    return redisService.get(key).flatMap(value -> {
      try {
        return Optional.ofNullable(objectMapper.readValue(value, type));
      } catch (Exception ex) {
        return Optional.empty();
      }
    });
  }

  public void store(String key, Object value, Duration ttl) {
    try {
      String json = objectMapper.writeValueAsString(value);
      redisService.set(key, json, ttl);
    } catch (Exception ex) {
      // best-effort
    }
  }
}
