package com.easynutri.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.data.model.Models.DashboardDto;
import com.easynutri.data.model.Models.MealPlanItemDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentHomeBinding;
import com.easynutri.ui.achievements.AchievementsActivity;
import com.easynutri.ui.activity.ActivityTrackingActivity;
import com.easynutri.ui.assistant.AssistantActivity;
import com.easynutri.ui.community.CommunityActivity;
import com.easynutri.ui.cooking.CookingActivity;
import com.easynutri.ui.hydration.HydrationActivity;
import com.easynutri.ui.pantry.PantryActivity;
import com.google.android.material.card.MaterialCardView;
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
    binding.tvGreeting.setText(getString(R.string.home_greeting, name));

    SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault());
    binding.tvTodayDate.setText(sdf.format(new Date()));

    boolean sensitive = repository.getSessionManager().isSensitiveMode();
    binding.cardSensitiveModeBanner.setVisibility(sensitive ? View.VISIBLE : View.GONE);
  }

  private void setupActions() {
    binding.cardStreak.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), AchievementsActivity.class));
        });

    binding.cardWater.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), HydrationActivity.class));
        });

    binding.btnQuickWater.setOnClickListener(
        v -> {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
          repository.logWater(
              250,
              sdf.format(new Date()),
              "Quick log 250ml",
              new AppRepository.DataCallback<com.easynutri.data.model.Models.WaterLogDto>() {
                @Override
                public void onSuccess(com.easynutri.data.model.Models.WaterLogDto data) {
                  Toast.makeText(requireContext(), "Logged +250ml water!", Toast.LENGTH_SHORT)
                      .show();
                  loadDashboard();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Water log: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    binding.cardSteps.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), ActivityTrackingActivity.class));
        });

    binding.btnQuickSteps.setOnClickListener(
        v -> {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
          repository.logSteps(
              1000,
              sdf.format(new Date()),
              new AppRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                  Toast.makeText(requireContext(), "Logged +1,000 steps!", Toast.LENGTH_SHORT)
                      .show();
                  loadDashboard();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Steps log: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    binding.btnActionPantry.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), PantryActivity.class);
          intent.putExtra("initial_tab", "pantry");
          startActivity(intent);
        });

    binding.btnActionGrocery.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), PantryActivity.class);
          intent.putExtra("initial_tab", "grocery");
          startActivity(intent);
        });

    binding.btnActionCook.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), CookingActivity.class));
        });

    binding.btnActionAssistant.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), AssistantActivity.class));
        });

    binding.btnActionCommunity.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), CommunityActivity.class));
        });

    binding.btnActionAchievements.setOnClickListener(
        v -> {
          startActivity(new Intent(requireContext(), AchievementsActivity.class));
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
            // Non-fatal if offline: cached values or defaults will show
          }
        });
  }

  private void renderDashboard(DashboardDto data) {
    boolean sensitive =
        (data.profile != null && data.profile.sensitiveMode)
            || repository.getSessionManager().isSensitiveMode();
    binding.cardSensitiveModeBanner.setVisibility(sensitive ? View.VISIBLE : View.GONE);

    if (sensitive) {
      binding.tvCalorieBalance.setText("Mindful Nourishment");
      binding.progressCalories.setProgress(50);
      binding.tvProteinValue.setText("Optimal");
      binding.tvCarbsValue.setText("Balanced");
      binding.tvFatValue.setText("Wholesome");
    } else {
      int consumed = data.consumedCalories != null ? data.consumedCalories : 0;
      int target = data.targetCalories != null ? data.targetCalories : 2000;
      binding.tvCalorieBalance.setText(consumed + " / " + target + " kcal");
      if (target > 0) {
        int percent = Math.min(100, (int) ((consumed * 100.0) / target));
        binding.progressCalories.setProgress(percent);
      }
      binding.tvProteinValue.setText(
          (data.consumedProteinG != null ? data.consumedProteinG : 0) + "g");
      binding.tvCarbsValue.setText((data.consumedCarbG != null ? data.consumedCarbG : 0) + "g");
      binding.tvFatValue.setText((data.consumedFatG != null ? data.consumedFatG : 0) + "g");
    }

    double consumedWater = data.consumedWaterL != null ? data.consumedWaterL : 0.0;
    double targetWater = data.targetWaterL != null ? data.targetWaterL : 2.5;
    binding.tvWaterDisplay.setText(
        String.format(Locale.US, "%.1f / %.1f L", consumedWater, targetWater));
    int steps = data.currentSteps != null ? data.currentSteps : 0;
    int targetSteps = data.targetSteps != null ? data.targetSteps : 8000;
    binding.tvStepsDisplay.setText(steps + " / " + targetSteps);
    binding.tvStreakCount.setText(data.activeStreakDays + " Day Streak");

    // Render today's meals
    binding.layoutTodayMeals.removeAllViews();
    if (data.todayMeals != null && !data.todayMeals.isEmpty()) {
      for (MealPlanItemDto meal : data.todayMeals) {
        addMealView(meal);
      }
    } else {
      TextView tvEmpty = new TextView(requireContext());
      tvEmpty.setText("No meals planned yet for today. Use Planner to generate your meals.");
      tvEmpty.setTextColor(getResources().getColor(R.color.text_secondary, null));
      tvEmpty.setTextSize(13);
      tvEmpty.setPadding(8, 8, 8, 8);
      binding.layoutTodayMeals.addView(tvEmpty);
    }
  }

  private void addMealView(MealPlanItemDto meal) {
    MaterialCardView card = new MaterialCardView(requireContext());
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 8, 0, 8);
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getResources().getColor(R.color.brand_surface, null));
    card.setStrokeColor(getResources().getColor(R.color.outline_subtle, null));
    card.setStrokeWidth(2);
    card.setRadius(16);

    LinearLayout container = new LinearLayout(requireContext());
    container.setOrientation(LinearLayout.HORIZONTAL);
    container.setPadding(24, 20, 24, 20);
    container.setGravity(android.view.Gravity.CENTER_VERTICAL);

    LinearLayout textLayout = new LinearLayout(requireContext());
    textLayout.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams textParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    textLayout.setLayoutParams(textParams);

    TextView title = new TextView(requireContext());
    String rTitle =
        (meal.recipe != null && meal.recipe.titleEn != null)
            ? meal.recipe.titleEn
            : "Balanced Meal";
    String slot = meal.mealSlot != null ? meal.mealSlot : "Meal";
    title.setText(slot + ": " + rTitle);
    title.setTextColor(getResources().getColor(R.color.text_primary, null));
    title.setTextSize(15);
    title.setTypeface(null, android.graphics.Typeface.BOLD);

    TextView sub = new TextView(requireContext());
    int cal = meal.recipe != null ? (int) meal.recipe.caloriesKcal : 350;
    sub.setText((meal.completed ? "✓ Completed" : "○ Scheduled") + " · " + cal + " kcal");
    sub.setTextColor(
        getResources()
            .getColor(meal.completed ? R.color.status_success : R.color.text_secondary, null));
    sub.setTextSize(12);

    textLayout.addView(title);
    textLayout.addView(sub);
    container.addView(textLayout);

    card.addView(container);
    binding.layoutTodayMeals.addView(card);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
