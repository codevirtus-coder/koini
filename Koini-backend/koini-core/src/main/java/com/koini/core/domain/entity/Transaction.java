package com.koini.core.domain.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.koini.core.domain.enums.TransactionStatus;
import com.koini.core.domain.enums.TransactionType;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "tx_id", nullable = false, updatable = false)
  private UUID txId;

  @Enumerated(EnumType.STRING)
  @Column(name = "tx_type", nullable = false, length = 30)
  private TransactionType txType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "from_wallet_id")
  private Wallet fromWallet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "to_wallet_id")
  private Wallet toWallet;

  @Column(name = "amount_kc", nullable = false)
  private long amountKc;

  @Column(name = "fee_kc", nullable = false)
  private long feeKc;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TransactionStatus status;

  @Column(name = "reference", nullable = false, unique = true, length = 100)
  private String reference;

  @Column(name = "description", length = 255)
  private String description;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "initiated_by")
  private User initiatedBy;

  @Column(name = "route_id")
  private UUID routeId;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private JsonNode metadata;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
