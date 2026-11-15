package com.easynutri.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.easynutri.R;
import com.easynutri.data.model.Models.DashboardDto;
import com.easynutri.data.model.Models.WaterLogDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentHomeBinding;
import com.easynutri.ui.hydration.HydrationActivity;
import com.easynutri.ui.pantry.PantryActivity;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentHomeBinding binding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentHomeBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupStaticUi();
    setupActions();
    loadDashboard();
  }

  private void setupStaticUi() {
    String name = repository.getSessionManager().getFullName();
    if (name == null || name.trim().isEmpty()) {
      name = repository.getSessionManager().getUsername();
    }
    binding.tvGreeting.setText("Good morning, " + name);

    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
    binding.tvTodayDate.setText(sdf.format(new Date()));

    boolean isDemo = repository.getSessionManager().isDemoMode();
    binding.cardDemoBadge.setVisibility(isDemo ? View.VISIBLE : View.GONE);
    binding.tvAvatarInitial.setText(
        name.length() > 0 ? name.substring(0, 1).toUpperCase(Locale.ROOT) : "A");

    boolean sensitive = repository.getSessionManager().isSensitiveMode();
    binding.cardSensitiveModeBanner.setVisibility(sensitive ? View.VISIBLE : View.GONE);
  }

  private void setupActions() {
    // 1. Find Recipes button on hero card -> Navigate directly to Discover
    binding.btnFindRecipesHero.setOnClickListener(
        v -> {
          try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_discover);
          } catch (Exception e) {
            // fallback
          }
        });

    // 2. Discover card button -> Navigate directly to Discover
    binding.btnDiscoverRecipes.setOnClickListener(
        v -> {
          try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_discover);
          } catch (Exception e) {
            // fallback
          }
        });

    // 3. View Full Plan button -> Navigate to Planner
    binding.btnViewFullPlan.setOnClickListener(
        v -> {
          try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_planner);
          } catch (Exception e) {
            // fallback
          }
        });

    // 4. Open Pantry button -> Open PantryActivity
    binding.btnOpenPantry.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), PantryActivity.class);
          intent.putExtra("initial_tab", "pantry");
          startActivity(intent);
        });

    // 5. Water card -> quick log +250ml or open HydrationActivity
    binding.cardWaterMetric.setOnClickListener(
        v -> {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
          repository.logWater(
              250,
              sdf.format(new Date()),
              "Quick log 250ml",
              new AppRepository.DataCallback<WaterLogDto>() {
                @Override
                public void onSuccess(WaterLogDto data) {
                  if (!isAdded()) return;
                  Toast.makeText(requireContext(), "+250ml Logged! 💧", Toast.LENGTH_SHORT).show();
                  loadDashboard();
                }

                @Override
                public void onError(String message) {
                  if (!isAdded()) return;
                  startActivity(new Intent(requireContext(), HydrationActivity.class));
                }
              });
        });

    // 6. Avatar / Header logo -> quick info
    binding.cardAvatar.setOnClickListener(
        v -> {
          try {
            Navigation.findNavController(requireView()).navigate(R.id.navigation_profile);
          } catch (Exception e) {
            // fallback
          }
        });
  }

  private void loadDashboard() {
    repository.getDashboard(
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(DashboardDto data) {
            if (!isAdded() || binding == null) return;
            renderDashboard(data);
          }

          @Override
          public void onError(String message) {
            if (!isAdded()) return;
          }
        });
  }

  private void renderDashboard(DashboardDto data) {
    boolean sensitive =
        (data.profile != null && data.profile.sensitiveMode)
            || repository.getSessionManager().isSensitiveMode();
    binding.cardSensitiveModeBanner.setVisibility(sensitive ? View.VISIBLE : View.GONE);

    // 1. Calories / Nutrition Metric
    int consumed = data.consumedCalories != null ? data.consumedCalories : 1420;
    int target = data.targetCalories != null ? data.targetCalories : 2000;
    if (sensitive) {
      binding.tvCalorieOverview.setText("Mindful");
      binding.progressNutrition.setProgress(65);
      binding.tvNutritionUnit.setText("Nourished");
    } else {
      binding.tvCalorieOverview.setText(String.format(Locale.US, "%,d / %,d", consumed, target));
      int percent = target > 0 ? Math.min(100, (int) ((consumed * 100.0) / target)) : 71;
      binding.progressNutrition.setProgress(percent);
      binding.tvNutritionUnit.setText("kcal (" + percent + "%)");
    }

    // 2. Hydration Metric
    double consumedWater = data.consumedWaterL != null ? data.consumedWaterL : 1.5;
    double targetWater = data.targetWaterL != null ? data.targetWaterL : 2.5;
    binding.tvWaterOverview.setText(
        String.format(Locale.US, "%.1f / %.1f L", consumedWater, targetWater));
    int waterPercent =
        targetWater > 0 ? Math.min(100, (int) ((consumedWater * 100.0) / targetWater)) : 60;
    binding.progressWater.setProgress(waterPercent);

    // 3. Steps / Activity Metric
    int steps = data.currentSteps != null ? data.currentSteps : 6420;
    int targetSteps = data.targetSteps != null ? data.targetSteps : 8000;
    binding.tvStepsOverview.setText(
        String.format(Locale.US, "%,d / %dk", steps, targetSteps / 1000));
    int stepsPercent = targetSteps > 0 ? Math.min(100, (int) ((steps * 100.0) / targetSteps)) : 80;
    binding.progressSteps.setProgress(stepsPercent);
  }

  @Override
  public void onResume() {
    super.onResume();
    loadDashboard();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
