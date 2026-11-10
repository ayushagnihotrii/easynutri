package com.easynutri.ui.cooking;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.CookingSessionDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityCookingBinding;
import com.google.android.material.card.MaterialCardView;
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
    String passedTitle = getIntent().getStringExtra("recipe_title");
    if (passedTitle != null && !passedTitle.isEmpty()) {
      binding.tvRecipeTitle.setText(passedTitle);
    } else {
      binding.tvRecipeTitle.setText("Palak Paneer (Clinical Balanced)");
    }

    steps.add(
        "Heat a skillet on medium. Add 1 tsp cold-pressed mustard oil. Add cumin seeds and let them splutter.");
    steps.add(
        "Add chopped onions and ginger-garlic paste. Sauté until golden brown (approx 4-5 mins).");
    steps.add(
        "Stir in pureed blanched spinach and low-fat paneer cubes. Simmer for 5 minutes with low salt.");

    ingredients.add("Paneer (low-fat) - 150g");
    ingredients.add("Spinach (Palak) - 200g");
    ingredients.add("Onion & Garlic - 50g");
    ingredients.add("Cold-pressed mustard oil - 5ml");

    renderCurrentStep();
    renderIngredientsList();
  }

  private void renderCurrentStep() {
    binding.tvStepHeader.setText("Step " + (currentStepIndex + 1) + " of " + steps.size());
    binding.tvStepInstruction.setText(steps.get(currentStepIndex));
    binding.btnPrevStep.setEnabled(currentStepIndex > 0);
    binding.btnNextStep.setText(
        currentStepIndex == steps.size() - 1 ? "Finish Steps" : "Next Step");
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
            finishSession();
          }
        });

    binding.btnFinishCooking.setOnClickListener(v -> finishSession());
  }

  private void startSessionOnBackend() {
    String recipeId = getIntent().getStringExtra("recipe_id");
    if (recipeId == null || recipeId.isEmpty()) {
      recipeId = "recipe-palak-paneer";
    }
    repository.startCookingSession(
        recipeId,
        2,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(CookingSessionDto session) {
            sessionId = session.sessionId;
          }

          @Override
          public void onError(String message) {
            // Fallback session locally
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
              Toast.makeText(
                      CookingActivity.this,
                      "Cooking completed! Ingredients deducted from Smart Pantry.",
                      Toast.LENGTH_LONG)
                  .show();
              finish();
            }

            @Override
            public void onError(String message) {
              Toast.makeText(
                      CookingActivity.this, "Session finished: " + message, Toast.LENGTH_SHORT)
                  .show();
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
