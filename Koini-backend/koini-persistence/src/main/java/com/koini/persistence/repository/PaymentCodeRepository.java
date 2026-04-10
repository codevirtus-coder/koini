package com.koini.persistence.repository;

import com.koini.core.domain.entity.PaymentCode;
import com.koini.core.domain.enums.PaymentCodeStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCodeRepository extends JpaRepository<PaymentCode, UUID> {
  Optional<PaymentCode> findByCodeHashAndStatus(String hash, PaymentCodeStatus status);

  long countByStatus(PaymentCodeStatus status);

  @Query("SELECT pc FROM PaymentCode pc WHERE pc.status = 'PENDING' AND pc.expiresAt > :now")
  List<PaymentCode> findActiveCodes(@Param("now") LocalDateTime now);

  @Modifying
  @Query("UPDATE PaymentCode pc SET pc.status = 'EXPIRED' "
      + "WHERE pc.status = 'PENDING' AND pc.expiresAt < :now")
  int expireStaleCodesBeforeTime(@Param("now") LocalDateTime now);
}
