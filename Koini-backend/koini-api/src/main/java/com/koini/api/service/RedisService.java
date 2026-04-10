package com.koini.api.service;

import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

  private final StringRedisTemplate redisTemplate;

  public RedisService(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void set(String key, String value, Duration ttl) {
    redisTemplate.opsForValue().set(key, value, ttl);
  }

  public Optional<String> get(String key) {
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  public void delete(String key) {
    redisTemplate.delete(key);
  }

  public boolean exists(String key) {
    Boolean exists = redisTemplate.hasKey(key);
    return exists != null && exists;
  }

  public long increment(String key) {
    Long value = redisTemplate.opsForValue().increment(key);
    return value != null ? value : 0L;
  }

  public void expire(String key, Duration ttl) {
    redisTemplate.expire(key, ttl);
  }
}
