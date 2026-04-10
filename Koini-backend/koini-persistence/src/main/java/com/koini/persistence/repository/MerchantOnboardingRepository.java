package com.koini.persistence.repository;

import com.koini.core.domain.entity.MerchantOnboarding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantOnboardingRepository extends JpaRepository<MerchantOnboarding, UUID> {
  Optional<MerchantOnboarding> findByUserUserId(UUID userId);
}

