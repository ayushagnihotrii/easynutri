package com.easynutri.ui.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.MainActivity;
import com.easynutri.core.network.SessionManager;
import com.easynutri.data.demo.DemoDataManager;
import com.easynutri.data.model.Models.AuthResponse;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityWelcomeBinding;
import com.easynutri.ui.auth.LoginActivity;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class WelcomeActivity extends AppCompatActivity {

  @Inject SessionManager sessionManager;
  @Inject AppRepository repository;

  private ActivityWelcomeBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    if (sessionManager == null) {
      sessionManager = new SessionManager(this);
    }

    setupActions();
  }

  private void setupActions() {
    binding.btnExploreDemo.setOnClickListener(v -> enterDemoMode());
    binding.btnContinueGuest.setOnClickListener(v -> enterGuestMode());
    binding.btnSignIn.setOnClickListener(
        v -> {
          startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        });
  }

  private void enterDemoMode() {
    binding.progressBar.setVisibility(View.VISIBLE);
    binding.btnExploreDemo.setEnabled(false);

    // 1. Initialize presentation state in DemoDataManager
    DemoDataManager.getInstance(this).resetToBaseline();

    // 2. Establish local demo session
    sessionManager.startDemoSession();

    // 3. Authenticate with backend demo account in background for live cloud sync
    repository.login(
        "demo",
        "DevDemoPass2026!#",
        new AppRepository.DataCallback<AuthResponse>() {
          @Override
          public void onSuccess(AuthResponse data) {
            // Live session established with backend
            openDashboard("Connected to live demo cloud account");
          }

          @Override
          public void onError(String message) {
            // Safe fallback to isolated presentation demo
            openDashboard("Loaded presentation demo data");
          }
        });
  }

  private void enterGuestMode() {
    sessionManager.startGuestSession();
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  private void openDashboard(String notice) {
    if (isFinishing()) return;
    Toast.makeText(this, notice, Toast.LENGTH_SHORT).show();
    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }
}
