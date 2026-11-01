package com.easynutri.service;

import com.easynutri.dto.RegisterRequest;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public UserEntity registerUser(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new IllegalArgumentException("Username '" + request.username() + "' is already taken.");
    }
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("Email '" + request.email() + "' is already registered.");
    }

    String encodedPassword = passwordEncoder.encode(request.password());
    UserEntity user =
        new UserEntity(
            request.username(),
            request.email(),
            encodedPassword,
            request.fullName() != null && !request.fullName().isBlank()
                ? request.fullName()
                : request.username());

    return userRepository.save(user);
  }

  @Transactional(readOnly = true)
  public Optional<UserEntity> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Transactional(readOnly = true)
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }
}
