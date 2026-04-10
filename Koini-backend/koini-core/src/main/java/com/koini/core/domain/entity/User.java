package com.koini.core.domain.entity;

import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "phone", nullable = false, unique = true, length = 20)
  private String phone;

  @Column(name = "full_name", length = 150)
  private String fullName;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "pin_hash", length = 255)
  private String pinHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 30)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private UserStatus status;

  @Column(name = "pin_attempts", nullable = false)
  private short pinAttempts;

  @Column(name = "pin_locked_until")
  private LocalDateTime pinLockedUntil;

  @Column(name = "national_id", length = 50)
  private String nationalId;

  @Column(name = "kyc_level", nullable = false)
  private short kycLevel;

  @Column(name = "last_login")
  private LocalDateTime lastLogin;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToOne(mappedBy = "user", fetch = FetchType.LAZY)
  private Wallet wallet;
}
