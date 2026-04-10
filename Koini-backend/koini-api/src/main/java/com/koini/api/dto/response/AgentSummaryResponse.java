package com.koini.api.dto.response;

public record AgentSummaryResponse(
    String agentId,
    String userId,
    String businessName,
    long floatBalanceKc,
    String floatBalanceUsd,
    String status
) {
}
