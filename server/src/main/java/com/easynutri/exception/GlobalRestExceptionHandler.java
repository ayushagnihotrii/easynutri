package com.easynutri.exception;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for REST APIs providing clean, sanitized error payloads and
 * accurate HTTP status codes without leaking internal stack traces.
 */
@RestControllerAdvice(basePackages = "com.easynutri.controller.api")
public class GlobalRestExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalRestExceptionHandler.class);

  @ExceptionHandler(SecurityException.class)
  public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
    log.warn("Access denied on API endpoint: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    log.info("Invalid API request argument: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            Map.of(
                "timestamp", Instant.now().toString(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
    log.error("Unhandled API exception: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            Map.of(
                "timestamp",
                Instant.now().toString(),
                "status",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error",
                "Internal Server Error",
                "message",
                "An unexpected error occurred while processing the request."));
  }
}
