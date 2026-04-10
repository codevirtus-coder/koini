package com.koini.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koini.api.dto.request.RegisterPassengerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthControllerIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
      .withDatabaseName("koini")
      .withUsername("koini")
      .withPassword("koini");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

  @DynamicPropertySource
  static void properties(DynamicPropertyRegistry registry) {
    registry.add("KOINI_DB_URL", postgres::getJdbcUrl);
    registry.add("KOINI_DB_USERNAME", postgres::getUsername);
    registry.add("KOINI_DB_PASSWORD", postgres::getPassword);
    registry.add("KOINI_REDIS_HOST", redis::getHost);
    registry.add("KOINI_REDIS_PORT", () -> redis.getMappedPort(6379));
    registry.add("KOINI_JWT_SECRET", () -> "0123456789012345678901234567890123456789012345678901234567890123");
    registry.add("KOINI_ALLOWED_ORIGINS", () -> "http://localhost");
    registry.add("KOINI_SMS_USERNAME", () -> "test");
    registry.add("KOINI_SMS_API_KEY", () -> "test");
  }

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void register_success_returns201() throws Exception {
    RegisterPassengerRequest request = new RegisterPassengerRequest("0771234567", "password123", "Test");
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());
  }
}
