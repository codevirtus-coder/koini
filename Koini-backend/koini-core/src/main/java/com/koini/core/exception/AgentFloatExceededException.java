package com.koini.core.exception;

public class AgentFloatExceededException extends BaseKoiniException {
  public static final String ERROR_CODE = "AGENT_001";

  public AgentFloatExceededException(String message) {
    super(ERROR_CODE, message);
  }

  public AgentFloatExceededException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
