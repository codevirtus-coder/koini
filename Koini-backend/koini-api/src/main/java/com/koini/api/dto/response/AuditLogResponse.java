package com.koini.api.dto.response;

public record AuditLogResponse(
    String logId,
    String action,
    String entityType,
    String entityId,
    String actorId,
    String outcome,
    String createdAt
) {
}
