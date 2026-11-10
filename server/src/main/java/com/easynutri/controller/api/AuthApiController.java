package com.easynutri.controller.api;

import com.easynutri.dto.AuthResponse;
import com.easynutri.dto.LoginRequest;
import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
    try {
      UserEntity user = userService.registerUser(request);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              user.getUsername(), null, List.of(new SimpleGrantedAuthority(user.getRole())));
      SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
      securityContext.setAuthentication(authentication);
      SecurityContextHolder.setContext(securityContext);

      HttpSession session = httpRequest.getSession(true);
      session.setAttribute(
          HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(
              new AuthResponse(
                  true, "User registered successfully", user.getUsername(), "/dashboard"));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage(), null, null));
    }
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate user credentials and establish session")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody LoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    return userService
        .findByUsername(request.username())
        .filter(
            user ->
                user.isEnabled()
                    && passwordEncoder.matches(request.password(), user.getPasswordHash()))
        .map(
            user -> {
              UsernamePasswordAuthenticationToken authentication =
                  new UsernamePasswordAuthenticationToken(
                      user.getUsername(),
                      null,
                      List.of(new SimpleGrantedAuthority(user.getRole())));
              SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
              securityContext.setAuthentication(authentication);
              SecurityContextHolder.setContext(securityContext);

              HttpSession session = httpRequest.getSession(true);
              session.setAttribute(
                  HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                  securityContext);

              return ResponseEntity.ok(
                  new AuthResponse(
                      true, "Authentication successful", user.getUsername(), "/dashboard"));
            })
        .orElseGet(
            () ->
                ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, "Invalid username or password", null, null)));
  }

  @PostMapping("/logout")
  @Operation(summary = "Log out active mobile session")
  public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(Map.of("success", true, "message", "Logged out successfully"));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current authenticated user info")
  public ResponseEntity<?> getCurrentUser(Principal principal) {
    if (principal == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Not authenticated"));
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
