package com.easynutri.server.api;

import com.easynutri.server.dto.HealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Server liveness and diagnostic probe")
public class HealthController {

  @GetMapping("/health")
  @Operation(summary = "Health probe checking backend liveness")
  public ResponseEntity<HealthResponse> getHealth() {
    return ResponseEntity.ok(new HealthResponse("UP", Instant.now(), "1.0.0"));
  }
}
