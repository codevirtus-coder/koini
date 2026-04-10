package com.koini.persistence.repository;

import com.koini.core.domain.entity.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfig, String> {
}

