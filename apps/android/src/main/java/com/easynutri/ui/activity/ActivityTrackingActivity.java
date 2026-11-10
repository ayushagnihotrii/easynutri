package com.easynutri.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.ActivitySummaryDto;
import com.easynutri.data.model.Models.WorkoutLogDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityTrackingBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class ActivityTrackingActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityTrackingBinding binding;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityTrackingBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupActions();
    loadActivity();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupActions() {
    binding.btnAdd1000Steps.setOnClickListener(v -> logSteps(1000));
    binding.btnAdd2500Steps.setOnClickListener(v -> logSteps(2500));
    binding.btnAdd5000Steps.setOnClickListener(v -> logSteps(5000));
    binding.btnLogWorkout.setOnClickListener(v -> showLogWorkoutDialog());
  }

  private void logSteps(int stepCount) {
    String dateStr = dateFormat.format(new Date());
    repository.logSteps(
        stepCount,
        dateStr,
        new AppRepository.DataCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean resp) {
            Toast.makeText(
                    ActivityTrackingActivity.this,
                    "Logged +" + stepCount + " steps!",
                    Toast.LENGTH_SHORT)
                .show();
            loadActivity();
          }

          @Override
          public void onError(String message) {
            Toast.makeText(
                    ActivityTrackingActivity.this, "Steps error: " + message, Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void showLogWorkoutDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.dialog_log_workout_title);

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    Spinner spType = new Spinner(this);
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            new String[] {"BRISK_WALKING", "RUNNING", "YOGA", "CYCLING", "STRENGTH_TRAINING"});
    spType.setAdapter(adapter);
    layout.addView(spType);

    EditText etDuration = new EditText(this);
    etDuration.setHint("Duration (minutes, e.g. 30)");
    etDuration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    layout.addView(etDuration);

    builder.setView(layout);

    builder.setPositiveButton(
        R.string.btn_save,
        (dialog, which) -> {
          String type = spType.getSelectedItem().toString();
          int duration = 30;
          try {
            if (!etDuration.getText().toString().trim().isEmpty()) {
              duration = Integer.parseInt(etDuration.getText().toString().trim());
            }
          } catch (Exception ignored) {
          }

          String dateStr = dateFormat.format(new Date());
          repository.logWorkout(
              type,
              duration,
              dateStr,
              "Mobile App Workout",
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(WorkoutLogDto data) {
                  Toast.makeText(
                          ActivityTrackingActivity.this,
                          "Logged " + type + " (" + data.caloriesBurned + " kcal burned)!",
                          Toast.LENGTH_SHORT)
                      .show();
                  loadActivity();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(
                          ActivityTrackingActivity.this,
                          "Workout error: " + message,
                          Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }

  private void loadActivity() {
    String dateStr = dateFormat.format(new Date());
    repository.getActivitySummary(
        dateStr,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(ActivitySummaryDto summary) {
            renderActivity(summary);
          }

          @Override
          public void onError(String message) {
            // Fallback
          }
        });
  }

  private void renderActivity(ActivitySummaryDto summary) {
    binding.tvStepsCount.setText(String.format(Locale.US, "%,d", summary.steps));
    binding.tvBurnedCalories.setText((int) summary.totalExpenditureKcal + " kcal");

    binding.layoutWorkouts.removeAllViews();
    if (summary.workouts != null && !summary.workouts.isEmpty()) {
      for (WorkoutLogDto workout : summary.workouts) {
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

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(20, 16, 20, 16);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView type = new TextView(this);
        type.setText("🏃 " + workout.activityType + " (" + workout.durationMinutes + "m)");
        type.setTextColor(getColor(R.color.text_primary));
        type.setTextSize(14);
        type.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams typeParams =
            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        type.setLayoutParams(typeParams);

        TextView burn = new TextView(this);
        burn.setText("-" + workout.caloriesBurned + " kcal");
        burn.setTextColor(getColor(R.color.brand_accent));
        burn.setTextSize(13);
        burn.setTypeface(null, android.graphics.Typeface.BOLD);

        row.addView(type);
        row.addView(burn);
        card.addView(row);
        binding.layoutWorkouts.addView(card);
      }
    } else {
      TextView tvEmpty = new TextView(this);
      tvEmpty.setText("No workouts recorded today. Log a walk or exercise session above!");
      tvEmpty.setTextColor(getColor(R.color.text_secondary));
      tvEmpty.setTextSize(13);
      tvEmpty.setPadding(8, 16, 8, 16);
      binding.layoutWorkouts.addView(tvEmpty);
    }
  }
}
