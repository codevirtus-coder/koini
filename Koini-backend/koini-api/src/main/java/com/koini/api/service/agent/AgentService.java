package com.koini.api.service.agent;

import com.koini.api.dto.response.DailySummaryResponse;
import com.koini.api.dto.response.FloatBalanceResponse;
import com.koini.core.domain.entity.Agent;
import com.koini.core.domain.enums.TransactionType;
import com.koini.core.domain.valueobject.MoneyUtils;
import com.koini.core.exception.ResourceNotFoundException;
import com.koini.persistence.repository.AgentRepository;
import com.koini.persistence.repository.TransactionRepository;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

  private final AgentRepository agentRepository;
  private final TransactionRepository transactionRepository;

  public AgentService(AgentRepository agentRepository, TransactionRepository transactionRepository) {
    this.agentRepository = agentRepository;
    this.transactionRepository = transactionRepository;
  }

  /**
   * Returns an agent's float balance.
   */
  @PreAuthorize("hasRole('AGENT')")
  public FloatBalanceResponse getFloatBalance(UUID agentUserId) {
    Agent agent = agentRepository.findByUserUserId(agentUserId)
        .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));
    return new FloatBalanceResponse(agent.getFloatBalanceKc(), MoneyUtils.formatUsd(agent.getFloatBalanceKc()));
  }

  /**
   * Returns daily summary of topups and withdrawals for an agent.
   */
  @PreAuthorize("hasRole('AGENT')")
  public DailySummaryResponse getDailySummary(UUID agentUserId) {
    long topups = transactionRepository.sumByTypeTodayForInitiator(agentUserId, TransactionType.TOPUP);
    long withdrawals = transactionRepository.sumByTypeTodayForInitiator(agentUserId, TransactionType.WITHDRAWAL);
    return new DailySummaryResponse(topups, withdrawals,
        MoneyUtils.formatUsd(topups), MoneyUtils.formatUsd(withdrawals));
  }
}
