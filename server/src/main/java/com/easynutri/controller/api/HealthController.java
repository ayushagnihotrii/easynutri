package com.easynutri.controller.api;

import com.easynutri.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Application liveness and readiness probe")
public class HealthController {

  @GetMapping
  @Operation(summary = "Check backend service health")
  public ResponseEntity<HealthResponse> checkHealth() {
    return ResponseEntity.ok(HealthResponse.up());
  }
}
