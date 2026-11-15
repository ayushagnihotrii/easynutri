package com.easynutri.ui.cooking;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.demo.DemoDataManager;
import com.easynutri.data.model.Models.CookingSessionDto;
import com.easynutri.data.model.Models.RecipeDetailDto;
import com.easynutri.data.model.Models.ScaledIngredientDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityCookingBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class CookingActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityCookingBinding binding;
  private Long sessionId = null;
  private int currentStepIndex = 0;
  private final List<String> steps = new ArrayList<>();
  private final List<String> ingredients = new ArrayList<>();
  private String recipeId;
  private String recipeTitle;
  private int servings = 2;

  private CountDownTimer timer;
  private boolean isTimerRunning = false;
  private long timeLeftInMillis = 300000; // 5 minutes default

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityCookingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupStepsAndIngredients();
    setupTimer();
    setupActions();
    startSessionOnBackend();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupStepsAndIngredients() {
    recipeId = getIntent().getStringExtra("recipe_id");
    recipeTitle = getIntent().getStringExtra("recipe_title");
    servings = getIntent().getIntExtra("servings", 2);
    if (servings <= 0) servings = 2;

    ArrayList<String> passedSteps = getIntent().getStringArrayListExtra("steps");
    ArrayList<String> passedIngs = getIntent().getStringArrayListExtra("ingredients");

    if (passedSteps != null && !passedSteps.isEmpty()) {
      steps.clear();
      steps.addAll(passedSteps);
    }
    if (passedIngs != null && !passedIngs.isEmpty()) {
      ingredients.clear();
      ingredients.addAll(passedIngs);
    }

    if (steps.isEmpty()) {
      DemoDataManager ddm = DemoDataManager.getInstance(this);
      RecipeDetailDto r =
          ddm.getRecipeById(recipeId != null ? recipeId : "recipe_moong_khichdi", servings);
      if (r != null) {
        if (recipeTitle == null || recipeTitle.isEmpty()) {
          recipeTitle = r.titleEn;
        }
        if (r.instructions != null) {
          steps.addAll(r.instructions);
        }
        if (r.ingredients != null) {
          for (ScaledIngredientDto ing : r.ingredients) {
            ingredients.add(ing.nameEn + " - " + (int) ing.amountG + "g");
          }
        }
      }
    }

    if (steps.isEmpty()) {
      steps.add("Rinse and prepare all ingredients according to recipe specifications.");
      steps.add("Heat pot, add seasonings and sauté aromatics until fragrant.");
      steps.add("Simmer with water until thoroughly cooked and balanced.");
    }

    if (recipeTitle != null && !recipeTitle.isEmpty()) {
      binding.tvRecipeTitle.setText(recipeTitle);
    }
    binding.tvServingsInfo.setText("Servings: " + servings + " · Scaled ingredients ready");

    renderCurrentStep();
    renderIngredientsList();
  }

  private void renderCurrentStep() {
    binding.tvStepHeader.setText("Step " + (currentStepIndex + 1) + " of " + steps.size());
    String currentText = steps.get(currentStepIndex);
    binding.tvStepInstruction.setText(currentText.replaceFirst("^\\d+\\.\\s*", ""));
    binding.btnPrevStep.setEnabled(currentStepIndex > 0);
    binding.btnNextStep.setText(
        currentStepIndex == steps.size() - 1 ? "Finish Cooking" : "Next Step");
  }

  private void renderIngredientsList() {
    binding.layoutCookingIngredients.removeAllViews();
    for (String ing : ingredients) {
      MaterialCardView card = new MaterialCardView(this);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 4, 0, 4);
      card.setLayoutParams(params);
      card.setCardBackgroundColor(getColor(R.color.brand_surface));
      card.setStrokeColor(getColor(R.color.outline_subtle));
      card.setStrokeWidth(1);
      card.setRadius(12);

      CheckBox cb = new CheckBox(this);
      cb.setText(ing);
      cb.setTextColor(getColor(R.color.text_primary));
      cb.setPadding(16, 12, 16, 12);
      card.addView(cb);
      binding.layoutCookingIngredients.addView(card);
    }
  }

  private void setupTimer() {
    updateTimerText();

    binding.btnTimerToggle.setOnClickListener(
        v -> {
          if (isTimerRunning) {
            pauseTimer();
          } else {
            startTimer();
          }
        });

    binding.btnTimerReset.setOnClickListener(v -> resetTimer());
  }

  private void startTimer() {
    timer =
        new CountDownTimer(timeLeftInMillis, 1000) {
          @Override
          public void onTick(long millisUntilFinished) {
            timeLeftInMillis = millisUntilFinished;
            updateTimerText();
          }

          @Override
          public void onFinish() {
            isTimerRunning = false;
            binding.btnTimerToggle.setText("Start Timer");
            Toast.makeText(
                    CookingActivity.this,
                    "Step timer complete! Proceeding to next step.",
                    Toast.LENGTH_LONG)
                .show();
          }
        }.start();

    isTimerRunning = true;
    binding.btnTimerToggle.setText("Pause");
  }

  private void pauseTimer() {
    if (timer != null) timer.cancel();
    isTimerRunning = false;
    binding.btnTimerToggle.setText("Resume");
  }

  private void resetTimer() {
    if (timer != null) timer.cancel();
    isTimerRunning = false;
    timeLeftInMillis = 300000;
    updateTimerText();
    binding.btnTimerToggle.setText("Start Timer");
  }

  private void updateTimerText() {
    int minutes = (int) (timeLeftInMillis / 1000) / 60;
    int seconds = (int) (timeLeftInMillis / 1000) % 60;
    binding.tvTimerDisplay.setText(
        String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds));
  }

  private void setupActions() {
    binding.btnPrevStep.setOnClickListener(
        v -> {
          if (currentStepIndex > 0) {
            currentStepIndex--;
            renderCurrentStep();
            updateBackendStep();
          }
        });

    binding.btnNextStep.setOnClickListener(
        v -> {
          if (currentStepIndex < steps.size() - 1) {
            currentStepIndex++;
            renderCurrentStep();
            updateBackendStep();
          } else {
            showCompletionDialog();
          }
        });

    binding.btnFinishCooking.setOnClickListener(v -> showCompletionDialog());
  }

  private void showCompletionDialog() {
    String title = recipeTitle != null ? recipeTitle : "Meal";
    new MaterialAlertDialogBuilder(this)
        .setTitle("Your meal is ready! 🍲")
        .setMessage(
            "You have successfully prepared "
                + title
                + " ("
                + servings
                + " servings).\n\nWhat would you like to do next?")
        .setPositiveButton(
            "Log Meal in Diary",
            (dialog, which) -> {
              DemoDataManager.getInstance(this)
                  .logMealToDiary(recipeId, servings, "Completed Meal");
              Toast.makeText(this, "Logged " + title + " to Diary! 🌿", Toast.LENGTH_LONG).show();
              finishSession();
            })
        .setNeutralButton(
            "Mark Completed",
            (dialog, which) -> {
              Toast.makeText(this, "Marked as completed!", Toast.LENGTH_SHORT).show();
              finishSession();
            })
        .setNegativeButton(
            "Return to Discover",
            (dialog, which) -> {
              finishSession();
            })
        .setCancelable(false)
        .show();
  }

  private void startSessionOnBackend() {
    if (recipeId == null || recipeId.isEmpty()) {
      recipeId = "recipe_moong_khichdi";
    }
    repository.startCookingSession(
        recipeId,
        servings,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(CookingSessionDto session) {
            sessionId = session.sessionId;
          }

          @Override
          public void onError(String message) {
            sessionId = 1L;
          }
        });
  }

  private void updateBackendStep() {
    if (sessionId != null) {
      repository.updateCookingStep(
          sessionId,
          currentStepIndex + 1,
          new AppRepository.DataCallback<>() {
            @Override
            public void onSuccess(CookingSessionDto data) {}

            @Override
            public void onError(String message) {}
          });
    }
  }

  private void finishSession() {
    if (sessionId != null) {
      repository.finishCookingSession(
          sessionId,
          true,
          new AppRepository.DataCallback<CookingSessionDto>() {
            @Override
            public void onSuccess(CookingSessionDto data) {
              finish();
            }

            @Override
            public void onError(String message) {
              finish();
            }
          });
    } else {
      finish();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (timer != null) timer.cancel();
  }
}
