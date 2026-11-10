package com.easynutri.ui.planner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.data.model.Models.MealPlanItemDto;
import com.easynutri.data.model.Models.WeeklyMealPlanResponseDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentPlannerBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

@AndroidEntryPoint
public class PlannerFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentPlannerBinding binding;
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
    binding = FragmentPlannerBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    setupDateNavigation();
    setupActions();
    loadPlanForSelectedDate();
  }

  private void setupDateNavigation() {
    updateDateDisplay();

    binding.btnPrevDay.setOnClickListener(
        v -> {
          selectedCalendar.add(Calendar.DAY_OF_YEAR, -1);
          updateDateDisplay();
          loadPlanForSelectedDate();
        });

    binding.btnNextDay.setOnClickListener(
        v -> {
          selectedCalendar.add(Calendar.DAY_OF_YEAR, 1);
          updateDateDisplay();
          loadPlanForSelectedDate();
        });
  }

  private void updateDateDisplay() {
    binding.tvPlanDate.setText(displayDateFormat.format(selectedCalendar.getTime()));
  }

  private void setupActions() {
    binding.btnGeneratePlan.setOnClickListener(
        v -> {
          binding.progressPlanner.setVisibility(View.VISIBLE);
          String currentDate = apiDateFormat.format(selectedCalendar.getTime());
          repository.generateWeeklyPlan(
              currentDate,
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(WeeklyMealPlanResponseDto data) {
                  if (!isAdded() || binding == null) return;
                  binding.progressPlanner.setVisibility(View.GONE);
                  Toast.makeText(
                          requireContext(),
                          "Balanced week generated successfully!",
                          Toast.LENGTH_SHORT)
                      .show();
                  loadPlanForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  if (!isAdded() || binding == null) return;
                  binding.progressPlanner.setVisibility(View.GONE);
                  Toast.makeText(requireContext(), "Generate plan: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });
  }

  private void loadPlanForSelectedDate() {
    String dateStr = apiDateFormat.format(selectedCalendar.getTime());
    binding.progressPlanner.setVisibility(View.VISIBLE);
    binding.tvEmptyPlan.setVisibility(View.GONE);

    repository.getMealPlan(
        dateStr,
        new AppRepository.DataCallback<List<MealPlanItemDto>>() {
          @Override
          public void onSuccess(List<MealPlanItemDto> items) {
            if (!isAdded() || binding == null) return;
            binding.progressPlanner.setVisibility(View.GONE);
            renderMealSlots(items);
          }

          @Override
          public void onError(String message) {
            if (!isAdded() || binding == null) return;
            binding.progressPlanner.setVisibility(View.GONE);
            binding.tvEmptyPlan.setVisibility(View.VISIBLE);
          }
        });
  }

  private void renderMealSlots(List<MealPlanItemDto> items) {
    binding.layoutSlots.removeAllViews();
    if (items == null || items.isEmpty()) {
      binding.tvEmptyPlan.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmptyPlan.setVisibility(View.GONE);

    for (MealPlanItemDto item : items) {
      addSlotCard(item);
    }
  }

  private void addSlotCard(MealPlanItemDto item) {
    MaterialCardView card = new MaterialCardView(requireContext());
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 10, 0, 10);
    card.setLayoutParams(params);
    card.setCardBackgroundColor(getResources().getColor(R.color.brand_surface, null));
    card.setStrokeColor(getResources().getColor(R.color.outline_subtle, null));
    card.setStrokeWidth(2);
    card.setRadius(16);

    LinearLayout container = new LinearLayout(requireContext());
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(24, 20, 24, 20);

    // Slot Header
    LinearLayout header = new LinearLayout(requireContext());
    header.setOrientation(LinearLayout.HORIZONTAL);
    header.setGravity(android.view.Gravity.CENTER_VERTICAL);

    TextView slotTitle = new TextView(requireContext());
    slotTitle.setText(item.mealSlot != null ? item.mealSlot.toUpperCase() : "MEAL");
    slotTitle.setTextColor(getResources().getColor(R.color.brand_primary, null));
    slotTitle.setTextSize(12);
    slotTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    LinearLayout.LayoutParams slotParams =
        new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
    slotTitle.setLayoutParams(slotParams);

    TextView statusText = new TextView(requireContext());
    statusText.setText(item.completed ? "✓ Eaten" : (item.locked ? "🔒 Locked" : ""));
    statusText.setTextColor(
        getResources()
            .getColor(item.completed ? R.color.status_success : R.color.text_secondary, null));
    statusText.setTextSize(12);

    header.addView(slotTitle);
    header.addView(statusText);
    container.addView(header);

    // Meal Title & Calories
    TextView mealTitle = new TextView(requireContext());
    String recipeName =
        (item.recipe != null && item.recipe.titleEn != null)
            ? item.recipe.titleEn
            : "Custom Food Entry";
    mealTitle.setText(recipeName);
    mealTitle.setTextColor(getResources().getColor(R.color.text_primary, null));
    mealTitle.setTextSize(16);
    mealTitle.setTypeface(null, android.graphics.Typeface.BOLD);
    mealTitle.setPadding(0, 6, 0, 4);
    container.addView(mealTitle);

    TextView calText = new TextView(requireContext());
    int cal = item.recipe != null ? (int) item.recipe.caloriesKcal : 350;
    calText.setText(cal + " kcal · Clinical Macro Target");
    calText.setTextColor(getResources().getColor(R.color.text_secondary, null));
    calText.setTextSize(12);
    container.addView(calText);

    // Action Buttons Row (Swap, Lock, Done)
    LinearLayout actionsRow = new LinearLayout(requireContext());
    actionsRow.setOrientation(LinearLayout.HORIZONTAL);
    actionsRow.setPadding(0, 16, 0, 0);

    Button btnSwap =
        new Button(
            requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
    btnSwap.setText("Swap");
    btnSwap.setTextSize(11);
    btnSwap.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1.0f));
    btnSwap.setOnClickListener(
        v -> {
          repository.swapMealPlanItem(
              item.id,
              "Balanced Swap Option",
              new AppRepository.DataCallback<>() {
                @Override
                public void onSuccess(MealPlanItemDto data) {
                  Toast.makeText(requireContext(), "Meal swapped successfully!", Toast.LENGTH_SHORT)
                      .show();
                  loadPlanForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Swap failed: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    Button btnLock =
        new Button(
            requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
    btnLock.setText(item.locked ? "Unlock" : "Lock");
    btnLock.setTextSize(11);
    LinearLayout.LayoutParams lockParams = new LinearLayout.LayoutParams(0, 40, 1.0f);
    lockParams.setMargins(8, 0, 8, 0);
    btnLock.setLayoutParams(lockParams);
    btnLock.setOnClickListener(
        v -> {
          repository.toggleLock(
              item.id,
              new AppRepository.DataCallback<MealPlanItemDto>() {
                @Override
                public void onSuccess(MealPlanItemDto data) {
                  loadPlanForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Lock error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    Button btnDone =
        new Button(
            requireContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
    btnDone.setText(item.completed ? "Undo" : "Done");
    btnDone.setTextSize(11);
    btnDone.setLayoutParams(new LinearLayout.LayoutParams(0, 40, 1.0f));
    btnDone.setOnClickListener(
        v -> {
          repository.toggleComplete(
              item.id,
              new AppRepository.DataCallback<MealPlanItemDto>() {
                @Override
                public void onSuccess(MealPlanItemDto data) {
                  loadPlanForSelectedDate();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(requireContext(), "Complete error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    actionsRow.addView(btnSwap);
    actionsRow.addView(btnLock);
    actionsRow.addView(btnDone);
    container.addView(actionsRow);

    card.addView(container);
    binding.layoutSlots.addView(card);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
