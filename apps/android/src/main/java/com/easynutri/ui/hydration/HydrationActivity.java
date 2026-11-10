package com.easynutri.ui.hydration;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.HydrationSummaryDto;
import com.easynutri.data.model.Models.WaterLogDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityHydrationBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class HydrationActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityHydrationBinding binding;
  private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityHydrationBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupActions();
    loadHydration();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupActions() {
    binding.btnAdd150.setOnClickListener(v -> logWater(150, "Cup of water"));
    binding.btnAdd250.setOnClickListener(v -> logWater(250, "Glass of water"));
    binding.btnAdd350.setOnClickListener(v -> logWater(350, "Mug of water"));
    binding.btnAdd500.setOnClickListener(v -> logWater(500, "Bottle of water"));
  }

  private void logWater(int amountMl, String note) {
    String dateStr = dateFormat.format(new Date());
    repository.logWater(
        amountMl,
        dateStr,
        note,
        new AppRepository.DataCallback<WaterLogDto>() {
          @Override
          public void onSuccess(WaterLogDto data) {
            Toast.makeText(
                    HydrationActivity.this, "Logged +" + amountMl + "ml!", Toast.LENGTH_SHORT)
                .show();
            loadHydration();
          }

          @Override
          public void onError(String message) {
            Toast.makeText(HydrationActivity.this, "Water error: " + message, Toast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void loadHydration() {
    String dateStr = dateFormat.format(new Date());
    repository.getHydration(
        dateStr,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(HydrationSummaryDto summary) {
            renderHydration(summary);
          }

          @Override
          public void onError(String message) {
            // Fallback default
          }
        });
  }

  private void renderHydration(HydrationSummaryDto summary) {
    int target = summary.dailyTargetMl > 0 ? summary.dailyTargetMl : 2500;
    binding.tvHydrationTotal.setText(summary.currentIntakeMl + " / " + target + " ml");

    int percent = Math.min(100, (int) ((summary.currentIntakeMl * 100.0) / target));
    binding.progressHydration.setProgress(percent);
    binding.tvHydrationPercent.setText(
        percent
            + "% of daily target reached ("
            + Math.max(0, target - summary.currentIntakeMl)
            + " ml remaining)");

    binding.layoutWaterLogs.removeAllViews();
    if (summary.logs != null && !summary.logs.isEmpty()) {
      for (WaterLogDto log : summary.logs) {
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

        TextView amount = new TextView(this);
        amount.setText("💧 +" + log.amountMl + " ml");
        amount.setTextColor(getColor(R.color.brand_primary));
        amount.setTextSize(14);
        amount.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams amountParams =
            new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        amount.setLayoutParams(amountParams);

        TextView note = new TextView(this);
        note.setText(log.loggedAt != null ? log.loggedAt : "Hydration");
        note.setTextColor(getColor(R.color.text_secondary));
        note.setTextSize(12);

        row.addView(amount);
        row.addView(note);
        card.addView(row);
        binding.layoutWaterLogs.addView(card);
      }
    } else {
      TextView tvEmpty = new TextView(this);
      tvEmpty.setText("No water logs yet today. Tap any button above to record intake.");
      tvEmpty.setTextColor(getColor(R.color.text_secondary));
      tvEmpty.setTextSize(13);
      tvEmpty.setPadding(8, 16, 8, 16);
      binding.layoutWaterLogs.addView(tvEmpty);
    }
  }
}
