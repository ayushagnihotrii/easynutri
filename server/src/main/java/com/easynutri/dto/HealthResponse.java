package com.easynutri.dto;

import java.time.Instant;

/** Standard API health status response. */
public record HealthResponse(String status, Instant timestamp, String version) {
  public static HealthResponse up() {
    return new HealthResponse("UP", Instant.now(), "1.0.0-phase0-web");
  }
}
