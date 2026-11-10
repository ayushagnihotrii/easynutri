package com.easynutri.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.MainActivity;
import com.easynutri.data.model.Models.AuthResponse;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityRegisterBinding;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

  @Inject AppRepository appRepository;

  private ActivityRegisterBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRegisterBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    binding.btnRegister.setOnClickListener(v -> performRegister());
    binding.tvLoginPrompt.setOnClickListener(v -> finish());
  }

  private void performRegister() {
    String fullName =
        binding.etFullName.getText() != null ? binding.etFullName.getText().toString().trim() : "";
    String username =
        binding.etUsername.getText() != null ? binding.etUsername.getText().toString().trim() : "";
    String email =
        binding.etEmail.getText() != null ? binding.etEmail.getText().toString().trim() : "";
    String password =
        binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";

    if (username.isEmpty() || email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
      showError("Please complete all registration fields");
      return;
    }

    if (password.length() < 8) {
      showError("Password must be at least 8 characters long");
      return;
    }

    binding.progressBar.setVisibility(View.VISIBLE);
    binding.tvError.setVisibility(View.GONE);
    binding.btnRegister.setEnabled(false);

    appRepository.register(
        username,
        email,
        password,
        fullName,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(AuthResponse data) {
            binding.progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
          }

          @Override
          public void onError(String message) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnRegister.setEnabled(true);
            showError(message);
          }
        });
  }

  private void showError(String message) {
    binding.tvError.setText(message);
    binding.tvError.setVisibility(View.VISIBLE);
  }
}
