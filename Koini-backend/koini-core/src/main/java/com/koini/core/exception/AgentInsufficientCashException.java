package com.koini.core.exception;

public class AgentInsufficientCashException extends BaseKoiniException {
  public static final String ERROR_CODE = "AGENT_002";

  public AgentInsufficientCashException(String message) {
    super(ERROR_CODE, message);
  }

  public AgentInsufficientCashException(String message, String details) {
    super(ERROR_CODE, message, details);
  }
}
