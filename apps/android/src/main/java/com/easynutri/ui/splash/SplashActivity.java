package com.easynutri.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.MainActivity;
import com.easynutri.core.network.SessionManager;
import com.easynutri.databinding.ActivitySplashBinding;
import com.easynutri.ui.welcome.WelcomeActivity;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class SplashActivity extends AppCompatActivity {

  @Inject SessionManager sessionManager;

  private ActivitySplashBinding binding;
  private final Handler handler = new Handler(Looper.getMainLooper());

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivitySplashBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    if (sessionManager == null) {
      sessionManager = new SessionManager(this);
    }

    // Logo entrance animation
    binding.layoutSplashContent.setAlpha(0.0f);
    binding.layoutSplashContent.setScaleX(0.75f);
    binding.layoutSplashContent.setScaleY(0.75f);

    binding
        .layoutSplashContent
        .animate()
        .alpha(1.0f)
        .scaleX(1.0f)
        .scaleY(1.0f)
        .setDuration(700)
        .setInterpolator(new AccelerateDecelerateInterpolator())
        .start();

    // Transition after smooth entrance
    handler.postDelayed(
        () -> {
          if (!isFinishing()) {
            if (sessionManager.isLoggedIn()) {
              startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
              startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            }
            finish();
          }
        },
        1200);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
  }
}
