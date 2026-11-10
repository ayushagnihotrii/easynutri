package com.easynutri.ui.diary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.data.model.Models.DiaryDayDto;
import com.easynutri.data.model.Models.DiaryItemDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentDiaryBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class DiaryFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentDiaryBinding binding;
  private final Calendar selectedCalendar = Calendar.getInstance();
  private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
  private final SimpleDateFormat displayDateFormat =
      new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentDiaryBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupDateNavigation();
    setupActions();
    loadDiaryForSelectedDate();
  }

  private void setupDateNavigation() {
    updateDateDisplay();

    binding.btnPrevDiaryDay.setOnClickListener(
        v -> {
          selectedCalendar.add(Calendar.DAY_OF_YEAR, -1);
          updateDateDisplay();
          loadDiaryForSelectedDate();
        });

    binding.btnNextDiaryDay.setOnClickListener(
        v -> {
          selectedCalendar.add(Calendar.DAY_OF_YEAR, 1);
          updateDateDisplay();
          loadDiaryForSelectedDate();
        });
  }

  private void updateDateDisplay() {
    binding.tvDiaryDate.setText(displayDateFormat.format(selectedCalendar.getTime()));
  }

  private void setupActions() {
    binding.btnLogFood.setOnClickListener(v -> showLogFoodDialog());

    binding.btnLogWaterModal.setOnClickListener(v -> showQuickWaterDialog());
  }

  private void showLogFoodDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(R.string.dialog_log_food_title);

    LinearLayout layout = new LinearLayout(requireContext());
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    EditText etFoodName = new EditText(requireContext());
    etFoodName.setHint("Food Name (e.g., Oatmeal, Dal Khichdi)");
    layout.addView(etFoodName);

    Spinner spSlot = new Spinner(requireContext());
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            new String[] {"BREAKFAST", "LUNCH", "DINNER", "SNACK"});
    spSlot.setAdapter(adapter);
    spSlot.setPadding(0, 16, 0, 16);
    layout.addView(spSlot);

    EditText etCalories = new EditText(requireContext());
    etCalories.setHint("Calories (kcal) - optional, default 250");
    etCalories.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
    layout.addView(etCalories);

    builder.setView(layout);

    builder.setPositiveButton(
        "Log",
        (dialog, which) -> {
          String foodName = etFoodName.getText().toString().trim();
          if (foodName.isEmpty()) {
            Toast.makeText(requireContext(), "Food name cannot be empty", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          String slot = spSlot.getSelectedItem().toString();
          int cal = 250;
          try {
            if (!etCalories.getText().toString().trim().isEmpty()) {
              cal = Integer.parseInt(etCalories.getText().toString().trim());
            }
          } catch (Exception ignored) {
          }

          String dateStr = apiDateFormat.format(selectedCalendar.getTime());
          repository.logDiaryItem(
              slot,
              foodName,
              1.0,
              "serving",
              cal,
              10.0,
              30.0,
              8.0,
              dateStr,
              new AppRepository.DataCallback<DiaryItemDto>() {
                @Override
                public void onSuccess(DiaryItemDto data) {
                  Toast.makeText(
                          requireContext(),
                          "Logged " + foodName + " to " + slot,
                          Toast.LENGTH_SHORT)
                      .show();
                  loadDiaryForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Log error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }

  private void showQuickWaterDialog() {
    String[] options = {
      "150 ml (Small cup)", "250 ml (Standard glass)", "350 ml (Mug)", "500 ml (Bottle)"
    };
    int[] amounts = {150, 250, 350, 500};

    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle(R.string.dialog_log_water_title);
    builder.setItems(
        options,
        (dialog, which) -> {
          int ml = amounts[which];
          String dateStr = apiDateFormat.format(selectedCalendar.getTime());
          repository.logWater(
              ml,
              dateStr,
              "Quick Water Log",
              new AppRepository.DataCallback<com.easynutri.data.model.Models.WaterLogDto>() {
                @Override
                public void onSuccess(com.easynutri.data.model.Models.WaterLogDto data) {
                  Toast.makeText(requireContext(), "Added " + ml + "ml water!", Toast.LENGTH_SHORT)
                      .show();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Water error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });
    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }

  private void loadDiaryForSelectedDate() {
    String dateStr = apiDateFormat.format(selectedCalendar.getTime());
    binding.progressDiary.setVisibility(View.VISIBLE);
    binding.tvEmptyDiary.setVisibility(View.GONE);

    repository.getDayDiary(
        dateStr,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(DiaryDayDto data) {
            if (!isAdded() || binding == null) return;
            binding.progressDiary.setVisibility(View.GONE);
            renderDiaryDay(data);
          }

          @Override
          public void onError(String message) {
            if (!isAdded() || binding == null) return;
            binding.progressDiary.setVisibility(View.GONE);
            binding.tvEmptyDiary.setVisibility(View.VISIBLE);
          }
        });
  }

  private void renderDiaryDay(DiaryDayDto data) {
    boolean sensitive = repository.getSessionManager().isSensitiveMode();
    if (sensitive) {
      binding.tvDiaryTotalCalories.setText("Balanced Intake");
      binding.tvDiaryProtein.setText("P: Nourished");
      binding.tvDiaryCarbs.setText("C: Optimal");
      binding.tvDiaryFat.setText("F: Wholesome");
    } else {
      binding.tvDiaryTotalCalories.setText((int) data.totalCalories + " kcal");
      binding.tvDiaryProtein.setText("P: " + (int) data.totalProteinG + "g");
      binding.tvDiaryCarbs.setText("C: " + (int) data.totalCarbsG + "g");
      binding.tvDiaryFat.setText("F: " + (int) data.totalFatG + "g");
    }

    binding.layoutDiaryItems.removeAllViews();
    List<DiaryItemDto> allItems = new java.util.ArrayList<>();
    if (data.slots != null) {
      for (com.easynutri.data.model.Models.DiarySlotDto slot : data.slots) {
        if (slot.items != null) {
          allItems.addAll(slot.items);
        }
      }
    }
    if (allItems.isEmpty()) {
      binding.tvEmptyDiary.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmptyDiary.setVisibility(View.GONE);

    for (DiaryItemDto item : allItems) {
      addDiaryItemCard(item, sensitive);
    }
  }

  private void addDiaryItemCard(DiaryItemDto item, boolean sensitive) {
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

    LinearLayout textCol = new LinearLayout(requireContext());
    textCol.setOrientation(LinearLayout.VERTICAL);
    LinearLayout.LayoutParams textParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    textCol.setLayoutParams(textParams);

    TextView title = new TextView(requireContext());
    title.setText(item.name != null ? item.name : "Food Item");
    title.setTextColor(getResources().getColor(R.color.text_primary, null));
    title.setTextSize(15);
    title.setTypeface(null, android.graphics.Typeface.BOLD);

    TextView sub = new TextView(requireContext());
    sub.setText((sensitive ? "Nourishing portion" : ((int) item.calories + " kcal")));
    sub.setTextColor(getResources().getColor(R.color.text_secondary, null));
    sub.setTextSize(12);

    textCol.addView(title);
    textCol.addView(sub);
    container.addView(textCol);

    Button btnDelete =
        new Button(
            requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
    btnDelete.setText("✕");
    btnDelete.setLayoutParams(new LinearLayout.LayoutParams(54, 40));
    btnDelete.setOnClickListener(
        v -> {
          repository.deleteDiaryItem(
              item.id,
              new AppRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean resp) {
                  Toast.makeText(requireContext(), "Item removed", Toast.LENGTH_SHORT).show();
                  loadDiaryForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Delete error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    container.addView(btnDelete);
    card.addView(container);
    binding.layoutDiaryItems.addView(card);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
