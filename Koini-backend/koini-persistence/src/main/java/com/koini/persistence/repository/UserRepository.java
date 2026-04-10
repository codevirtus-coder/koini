package com.koini.persistence.repository;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByPhone(String phone);

  boolean existsByPhone(String phone);

  Optional<User> findByFullNameIgnoreCaseAndRole(String fullName, UserRole role);

  Page<User> findByRoleAndStatus(UserRole role, UserStatus status, Pageable pageable);

  List<User> findByRoleInAndStatus(Collection<UserRole> roles, UserStatus status);

  long countByRole(UserRole role);

  long countByStatus(UserStatus status);
}
