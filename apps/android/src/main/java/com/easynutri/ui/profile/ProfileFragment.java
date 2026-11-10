package com.easynutri.ui.profile;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.data.model.Models.UserProfileDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentProfileBinding;
import com.easynutri.ui.auth.LoginActivity;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class ProfileFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentProfileBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentProfileBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupStaticUi();
    setupActions();
    loadProfile();
  }

  private void setupStaticUi() {
    String username = repository.getSessionManager().getUsername();
    String fullname = repository.getSessionManager().getFullName();

    binding.tvProfileUsername.setText("@" + username);
    binding.tvProfileFullname.setText(
        fullname != null && !fullname.isEmpty() ? fullname : username);

    boolean isSensitive = repository.getSessionManager().isSensitiveMode();
    binding.switchSensitiveMode.setChecked(isSensitive);
  }

  private void setupActions() {
    binding.switchSensitiveMode.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          repository.toggleSensitiveMode(
              isChecked,
              new AppRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean resp) {
                  Toast.makeText(
                          requireContext(),
                          isChecked
                              ? "Sensitive Mode Enabled: Mindful labels active"
                              : "Sensitive Mode Disabled",
                          Toast.LENGTH_SHORT)
                      .show();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(
                          requireContext(), "Sensitive mode error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    binding.btnToggleLanguage.setOnClickListener(
        v -> {
          String currentLang = repository.getSessionManager().getPreferredLanguage();
          String nextLang = "hi".equalsIgnoreCase(currentLang) ? "en" : "hi";
          repository.getSessionManager().setPreferredLanguage(nextLang);

          Locale locale = new Locale(nextLang);
          Locale.setDefault(locale);
          Configuration config = new Configuration();
          config.setLocale(locale);
          requireContext()
              .getResources()
              .updateConfiguration(config, requireContext().getResources().getDisplayMetrics());

          Toast.makeText(
                  requireContext(),
                  "Language changed to "
                      + (nextLang.equals("hi") ? "Hindi" : "English")
                      + ". Please restart app to apply globally.",
                  Toast.LENGTH_LONG)
              .show();
        });

    binding.btnLogout.setOnClickListener(
        v -> {
          repository.logout(
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(Boolean data) {
                  Intent intent = new Intent(requireContext(), LoginActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                  startActivity(intent);
                  requireActivity().finishAffinity();
                }

                @Override
                public void onError(String message) {
                  Intent intent = new Intent(requireContext(), LoginActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                  startActivity(intent);
                  requireActivity().finishAffinity();
                }
              });
        });
  }

  private void loadProfile() {
    repository.getProfile(
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(UserProfileDto profile) {
            if (!isAdded() || binding == null) return;
            renderProfile(profile);
          }

          @Override
          public void onError(String message) {
            // Fallback to local session data
          }
        });
  }

  private void renderProfile(UserProfileDto profile) {
    String fullName = repository.getSessionManager().getFullName();
    if (fullName != null && !fullName.isEmpty()) {
      binding.tvProfileFullname.setText(fullName);
    }
    if (profile.username != null) {
      binding.tvProfileUsername.setText("@" + profile.username);
      binding.tvProfileEmail.setText(profile.username + "@easynutri.app");
    }
    binding.tvTargetCalories.setText(
        (profile.targetCalories != null ? profile.targetCalories : 2000) + " kcal");
    binding.tvTargetWater.setText(
        (profile.targetWaterL != null ? profile.targetWaterL : 2.5) + " L");
    if (profile.dietaryPreference != null) {
      binding.tvDietType.setText("Diet: " + profile.dietaryPreference);
    }
    binding.switchSensitiveMode.setChecked(profile.sensitiveMode);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
