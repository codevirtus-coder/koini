package com.koini.persistence.repository;

import com.koini.core.domain.entity.PaymentRequest;
import com.koini.core.domain.enums.PaymentReqStatus;
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

  Optional<PaymentRequest> findByRequestIdAndConductorUserId(UUID requestId, UUID conductorId);

  @Query("SELECT pr FROM PaymentRequest pr "
      + "WHERE pr.conductor.userId = :conductorId "
      + "AND pr.createdAt >= :start AND pr.createdAt < :end "
      + "ORDER BY pr.createdAt DESC")
  Page<PaymentRequest> findForConductorBetween(
      @Param("conductorId") UUID conductorId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      Pageable pageable);

  @Query("SELECT COUNT(pr) FROM PaymentRequest pr "
      + "WHERE pr.conductor.userId = :conductorId "
      + "AND pr.createdAt >= :start AND pr.createdAt < :end")
  long countForConductorBetween(
      @Param("conductorId") UUID conductorId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT MAX(pr.createdAt) FROM PaymentRequest pr "
      + "WHERE pr.conductor.userId = :conductorId "
      + "AND pr.createdAt >= :start AND pr.createdAt < :end")
  LocalDateTime maxCreatedAtForConductorBetween(
      @Param("conductorId") UUID conductorId,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Query("SELECT pr FROM PaymentRequest pr "
      + "WHERE pr.conductor.userId = :conductorId "
      + "AND pr.status = :status "
      + "AND pr.createdAt >= :start AND pr.createdAt < :end "
      + "ORDER BY pr.createdAt DESC")
  Page<PaymentRequest> findForConductorBetweenWithStatus(
      @Param("conductorId") UUID conductorId,
      @Param("status") PaymentReqStatus status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end,
      Pageable pageable);

  @Query("SELECT COUNT(pr) FROM PaymentRequest pr "
      + "WHERE pr.conductor.userId = :conductorId "
      + "AND pr.status = :status "
      + "AND pr.createdAt >= :start AND pr.createdAt < :end")
  long countForConductorBetweenWithStatus(
      @Param("conductorId") UUID conductorId,
      @Param("status") PaymentReqStatus status,
      @Param("start") LocalDateTime start,
      @Param("end") LocalDateTime end);

  @Modifying
  @Query("UPDATE PaymentRequest pr SET pr.status = 'EXPIRED' "
      + "WHERE pr.status = 'PENDING' AND pr.expiresAt < :now")
  int expireStaleRequestsBeforeTime(@Param("now") LocalDateTime now);
}
