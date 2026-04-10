package com.koini.persistence.repository;

import com.koini.core.domain.entity.Agent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
  Optional<Agent> findByUserUserId(UUID userId);
}
