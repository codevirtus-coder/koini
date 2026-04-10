package com.koini.persistence.repository;

import com.koini.core.domain.entity.PaymentRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
  @Query("SELECT pr FROM PaymentRequest pr WHERE pr.requestId = :requestId AND pr.passenger.userId = :passengerId")
  Optional<PaymentRequest> findByRequestIdAndPassengerId(@Param("requestId") UUID requestId,
      @Param("passengerId") UUID passengerId);

  Optional<PaymentRequest> findByRequestIdAndPassengerUserId(UUID requestId, UUID passengerId);

  @Query("SELECT pr FROM PaymentRequest pr WHERE pr.conductor.userId = :conductorId ORDER BY pr.createdAt DESC")
  Page<PaymentRequest> findByConductorIdOrderByCreatedAtDesc(@Param("conductorId") UUID conductorId, Pageable pageable);

  Page<PaymentRequest> findByConductorUserIdOrderByCreatedAtDesc(UUID conductorId, Pageable pageable);

  @Modifying
  @Query("UPDATE PaymentRequest pr SET pr.status = 'EXPIRED' "
      + "WHERE pr.status = 'PENDING' AND pr.expiresAt < :now")
  int expireStaleRequestsBeforeTime(@Param("now") LocalDateTime now);
}
