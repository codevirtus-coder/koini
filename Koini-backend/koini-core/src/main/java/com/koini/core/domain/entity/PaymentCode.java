package com.koini.core.domain.entity;

import com.koini.core.domain.enums.PaymentCodeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "payment_codes")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCode implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "code_id", nullable = false, updatable = false)
  private UUID codeId;

  @Column(name = "code_hash", nullable = false, unique = true, length = 255)
  private String codeHash;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "holder_id", nullable = false)
  private User holder;

  @Column(name = "amount_kc", nullable = false)
  private long amountKc;

  @Column(name = "fee_kc", nullable = false)
  private long feeKc;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private PaymentCodeStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "redeemed_by")
  private User redeemedBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column(name = "redeemed_at")
  private LocalDateTime redeemedAt;
}
