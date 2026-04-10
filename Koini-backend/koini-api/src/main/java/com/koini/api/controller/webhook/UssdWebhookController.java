package com.koini.api.controller.webhook;

import com.koini.api.service.ussd.UssdService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/ussd")
public class UssdWebhookController {

  private final UssdService ussdService;

  public UssdWebhookController(UssdService ussdService) {
    this.ussdService = ussdService;
  }

  @Operation(summary = "USSD webhook", description = "Handles USSD session")
  @PostMapping(value = "", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
  public String handle(
      @RequestParam @NotBlank String sessionId,
      @RequestParam(required = false) String serviceCode,
      @RequestParam @NotBlank String phoneNumber,
      @RequestParam(required = false, defaultValue = "") String text) {
    try {
      return ussdService.handleSession(sessionId, phoneNumber, text);
    } catch (Exception ex) {
      return "END " + ex.getMessage();
    }
  }
}
