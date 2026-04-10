package com.koini.api.service.auth;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

  private final StringRedisTemplate redisTemplate;

  public RateLimitService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public boolean checkLimit(String key, int maxRequests, Duration window) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) {
      redisTemplate.expire(key, window);
    }
    return count != null && count <= maxRequests;
  }

  public void recordAttempt(String key, Duration window) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) {
      redisTemplate.expire(key, window);
    }
  }

  public void recordAttempt(String key) {
    recordAttempt(key, Duration.ofMinutes(15));
  }

  public boolean isLocked(String key) {
    Boolean exists = redisTemplate.hasKey(key);
    return exists != null && exists;
  }
}
