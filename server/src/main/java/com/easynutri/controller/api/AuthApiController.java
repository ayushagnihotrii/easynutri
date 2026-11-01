package com.easynutri.controller.api;

import com.easynutri.dto.AuthResponse;
import com.easynutri.dto.LoginRequest;
import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration API")
public class AuthApiController {

  private final UserService userService;
  private final PasswordEncoder passwordEncoder;

  public AuthApiController(UserService userService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.passwordEncoder = passwordEncoder;
  }

  @PostMapping("/register")
  @Operation(summary = "Register a new user account with hashed password storage")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    try {
      UserEntity user = userService.registerUser(request);
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              new AuthResponse(
                  true, "User registered successfully", user.getUsername(), "/dashboard"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage(), null, null));
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate user credentials")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return userService
        .findByUsername(request.username())
        .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
        .map(
            user ->
                ResponseEntity.ok(
                    new AuthResponse(
                        true, "Authentication successful", user.getUsername(), "/dashboard")))
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid username or password", null, null)));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current authenticated user info")
  public ResponseEntity<?> getCurrentUser(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }
    return userService
        .findByUsername(principal.getName())
        .map(
            user ->
                ResponseEntity.ok(
                    new UserInfoDto(
                        user.getUsername(), user.getEmail(), user.getFullName(), user.getRole())))
        .orElse(ResponseEntity.notFound().build());
  }

  public record UserInfoDto(String username, String email, String fullName, String role) {}
}
