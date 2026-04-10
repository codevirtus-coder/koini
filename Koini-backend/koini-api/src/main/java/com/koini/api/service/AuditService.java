package com.koini.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.koini.core.domain.entity.AuditLog;
import com.koini.core.domain.enums.AuditOutcome;
import com.koini.persistence.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

  private static final Logger log = LoggerFactory.getLogger(AuditService.class);
  private static final Set<String> SENSITIVE_KEYS = Set.of("password", "pin", "passwordHash",
      "pinHash", "nationalId");

  private final AuditLogRepository auditLogRepository;
  private final ObjectMapper objectMapper;

  public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
    this.auditLogRepository = auditLogRepository;
    this.objectMapper = objectMapper;
  }

  @Async
  public void log(
      String action,
      UUID actorId,
      String actorType,
      String entityType,
      String entityId,
      Object oldValue,
      Object newValue,
      AuditOutcome outcome,
      HttpServletRequest request
  ) {
    try {
      AuditLog auditLog = AuditLog.builder()
          .action(action)
          .actorId(actorId)
          .actorType(actorType)
          .entityType(entityType)
          .entityId(entityId)
          .ipAddress(resolveIp(request))
          .userAgent(request != null ? request.getHeader("User-Agent") : null)
          .oldValue(sanitize(oldValue))
          .newValue(sanitize(newValue))
          .outcome(outcome)
          .build();
      auditLogRepository.save(auditLog);
    } catch (StackOverflowError err) {
      log.error("Failed to write audit log: StackOverflowError (likely due to cyclic object graph)");
    } catch (Exception ex) {
      log.error("Failed to write audit log: {}", ex.getMessage());
    }
  }

  private String resolveIp(HttpServletRequest request) {
    if (request == null) {
      return null;
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      return forwarded.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private JsonNode sanitize(Object value) {
    if (value == null) {
      return null;
    }

    try {
      if (value instanceof Map<?, ?> mapValue) {
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
          Object k = entry.getKey();
          if (k == null) {
            continue;
          }
          String key = k.toString();
          if (SENSITIVE_KEYS.contains(key)) {
            continue;
          }
          sanitized.put(key, entry.getValue());
        }
        return objectMapper.valueToTree(sanitized);
      }

      if (isEntity(value)) {
        Map<String, Object> shallow = new HashMap<>();
        shallow.put("type", value.getClass().getSimpleName());
        Object id = tryExtractEntityId(value);
        if (id != null) {
          shallow.put("id", id.toString());
        }
        return objectMapper.valueToTree(shallow);
      }

      JsonNode node = objectMapper.valueToTree(value);
      if (node != null && node.isObject()) {
        for (String key : SENSITIVE_KEYS) {
          ((ObjectNode) node).remove(key);
        }
      }
      return node;
    } catch (StackOverflowError err) {
      return objectMapper.valueToTree(Map.of(
          "type", value.getClass().getSimpleName(),
          "error", "STACK_OVERFLOW"));
    } catch (Exception ex) {
      return objectMapper.valueToTree(Map.of(
          "type", value.getClass().getSimpleName(),
          "error", "SANITIZE_FAILED"));
    }
  }

  private boolean isEntity(Object value) {
    Package pkg = value.getClass().getPackage();
    String name = pkg != null ? pkg.getName() : "";
    return name.startsWith("com.koini.core.domain.entity");
  }

  private Object tryExtractEntityId(Object entity) {
    try {
      for (Method method : entity.getClass().getMethods()) {
        if (method.getParameterCount() != 0) {
          continue;
        }
        String name = method.getName();
        if ("getClass".equals(name)) {
          continue;
        }
        if ("getId".equals(name) || name.endsWith("Id")) {
          Object value = method.invoke(entity);
          if (value != null) {
            return value;
          }
        }
      }
      return null;
    } catch (Exception ex) {
      return null;
    }
  }
}
