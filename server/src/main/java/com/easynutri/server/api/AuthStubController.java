package com.easynutri.server.api;

import com.easynutri.server.dto.AuthPlaceholderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and session endpoints (Phase 6)")
public class AuthStubController {

  @PostMapping("/login")
  @Operation(summary = "Placeholder login endpoint")
  public ResponseEntity<AuthPlaceholderResponse> login() {
    return ResponseEntity.ok(
        new AuthPlaceholderResponse(
            "STUBBED",
            "Authentication endpoints will be fully implemented in Phase 6 (Community and Backend)."));
  }

  @PostMapping("/register")
  @Operation(summary = "Placeholder registration endpoint")
  public ResponseEntity<AuthPlaceholderResponse> register() {
    return ResponseEntity.ok(
        new AuthPlaceholderResponse(
            "STUBBED",
            "Account registration will be fully implemented in Phase 6 (Community and Backend)."));
  }
}
