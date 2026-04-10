package com.koini.api;

import com.koini.core.domain.entity.User;
import com.koini.core.domain.enums.UserRole;
import com.koini.core.domain.enums.UserStatus;
import com.koini.core.domain.valueobject.PhoneUtils;
import com.koini.persistence.repository.UserRepository;
import com.koini.security.jwt.JwtProperties;
import com.koini.api.config.PesepayProperties;
import com.koini.api.config.MoneyProperties;
import java.util.Arrays;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication(scanBasePackages = "com.koini")
@EnableJpaRepositories(basePackages = "com.koini.persistence.repository")
@EntityScan(basePackages = "com.koini.core.domain.entity")
@EnableConfigurationProperties({JwtProperties.class, PesepayProperties.class, MoneyProperties.class})
@EnableAsync
@EnableScheduling
public class KoiniApplication {

  public static void main(String[] args) {
    SpringApplication.run(KoiniApplication.class, args);
  }

  @Bean
  CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      String adminPhoneRaw = System.getenv().getOrDefault("KOINI_ADMIN_PHONE", "+27600000000");
      String adminPhoneNormalized = PhoneUtils.normalize(adminPhoneRaw);
      String adminName = System.getenv().getOrDefault("KOINI_ADMIN_USERNAME", "admin");
      String adminPassword = System.getenv().getOrDefault("KOINI_ADMIN_PASSWORD", "%$Pass123");
      boolean forceReset =
          Boolean.parseBoolean(System.getenv().getOrDefault("KOINI_ADMIN_RESET", "false"));

      User existing = userRepository.findByPhone(adminPhoneRaw)
          .or(() -> adminPhoneNormalized.equals(adminPhoneRaw) ? java.util.Optional.empty()
              : userRepository.findByPhone(adminPhoneNormalized))
          .or(() -> userRepository.findByFullNameIgnoreCaseAndRole(adminName, UserRole.ADMIN))
          .orElse(null);

      if (existing != null && !forceReset) {
        return;
      }

      UserRole role =
          Arrays.stream(UserRole.values())
              .filter(r -> r.name().equalsIgnoreCase("ADMIN"))
              .findFirst()
              .orElse(UserRole.values()[0]);

      UserStatus status =
          Arrays.stream(UserStatus.values())
              .filter(s -> s.name().equalsIgnoreCase("ACTIVE"))
              .findFirst()
              .orElse(UserStatus.values()[0]);

      User admin = existing != null ? existing : new User();
      admin.setPhone(adminPhoneRaw);
      admin.setFullName(adminName);
      admin.setPasswordHash(passwordEncoder.encode(adminPassword));
      admin.setRole(role);
      admin.setStatus(status);
      admin.setPinAttempts((short) 0);
      admin.setPinLockedUntil(null);
      admin.setKycLevel((short) 0);
      userRepository.save(admin);
    };
  }
}
