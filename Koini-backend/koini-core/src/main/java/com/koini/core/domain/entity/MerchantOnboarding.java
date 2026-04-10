package com.koini.core.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "merchant_onboarding")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOnboarding implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "onboarding_id", nullable = false, updatable = false)
  private UUID onboardingId;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "business_name", nullable = false, length = 200)
  private String businessName;

  @Column(name = "trading_name", nullable = false, length = 200)
  private String tradingName;

  @Column(name = "address_line1", nullable = false, length = 300)
  private String addressLine1;

  @Column(name = "city", nullable = false, length = 150)
  private String city;

  @Column(name = "country", nullable = false, length = 80)
  private String country;

  @Column(name = "id_number", nullable = false, length = 80)
  private String idNumber;

  @Column(name = "id_document_path", nullable = false, length = 500)
  private String idDocumentPath;

  @Column(name = "proof_of_address_path", nullable = false, length = 500)
  private String proofOfAddressPath;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}

