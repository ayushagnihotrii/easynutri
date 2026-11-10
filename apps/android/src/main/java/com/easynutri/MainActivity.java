package com.easynutri;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.easynutri.core.network.SessionManager;
import com.easynutri.databinding.ActivityMainBinding;
import com.easynutri.ui.auth.LoginActivity;
import com.easynutri.worker.SyncWorker;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

  @Inject SessionManager sessionManager;

  private ActivityMainBinding binding;
  private NavController navController;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (sessionManager == null) {
      sessionManager = new SessionManager(this);
    }

    if (!sessionManager.isLoggedIn()) {
      startActivity(new Intent(this, LoginActivity.class));
      finish();
      return;
    }

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    NavHostFragment navHostFragment =
        (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
    if (navHostFragment != null) {
      navController = navHostFragment.getNavController();
      NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
    }

    try {
      PeriodicWorkRequest syncWork =
          new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES).build();
      WorkManager.getInstance(this)
          .enqueueUniquePeriodicWork(
              "EasyNutriSyncWork", ExistingPeriodicWorkPolicy.KEEP, syncWork);
    } catch (Exception ignored) {
    }
  }
}
