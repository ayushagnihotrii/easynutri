package com.easynutri.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.MainActivity;
import com.easynutri.data.model.Models.AuthResponse;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityLoginBinding;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

  @Inject AppRepository appRepository;

  private ActivityLoginBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityLoginBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    String savedUrl = appRepository.getSessionManager().getBaseUrl();
    if (savedUrl != null && !savedUrl.isEmpty()) {
      binding.etServerUrl.setText(savedUrl);
    }

    binding.btnQuickDemo.setOnClickListener(
        v -> {
          binding.etUsername.setText("demo");
          binding.etPassword.setText("DevDemoPass2026!#");
        });

    binding.btnLogin.setOnClickListener(v -> performLogin());

    binding.tvRegisterPrompt.setOnClickListener(
        v -> {
          startActivity(new Intent(this, RegisterActivity.class));
        });
  }

  private void performLogin() {
    String username =
        binding.etUsername.getText() != null ? binding.etUsername.getText().toString().trim() : "";
    String password =
        binding.etPassword.getText() != null ? binding.etPassword.getText().toString().trim() : "";
    String serverUrl =
        binding.etServerUrl.getText() != null
            ? binding.etServerUrl.getText().toString().trim()
            : "";

    if (username.isEmpty() || password.isEmpty()) {
      showError("Please enter both username and password");
      return;
    }

    if (!serverUrl.isEmpty()) {
      appRepository.getSessionManager().setBaseUrl(serverUrl);
    }

    binding.progressBar.setVisibility(View.VISIBLE);
    binding.tvError.setVisibility(View.GONE);
    binding.btnLogin.setEnabled(false);

    appRepository.login(
        username,
        password,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(AuthResponse data) {
            binding.progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
          }

          @Override
          public void onError(String message) {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            showError(message);
          }
        });
  }

  private void showError(String message) {
    binding.tvError.setText(message);
    binding.tvError.setVisibility(View.VISIBLE);
  }
}
