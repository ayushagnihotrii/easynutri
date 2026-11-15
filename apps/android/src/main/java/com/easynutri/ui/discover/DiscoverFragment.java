package com.easynutri.ui.discover;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.data.demo.DemoDataManager;
import com.easynutri.data.model.Models.PantryItemDto;
import com.easynutri.data.model.Models.RecipeDetailDto;
import com.easynutri.data.model.Models.RecipeMatchItemDto;
import com.easynutri.data.model.Models.RecipeMatchResponseDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentDiscoverBinding;
import com.easynutri.ui.recipe.RecipeDetailActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.*;
import javax.inject.Inject;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentDiscoverBinding binding;
  private DemoDataManager demoDataManager;

  private final Set<String> selectedIngredientIds = new LinkedHashSet<>();
  private final Map<String, String> ingredientDisplayNames = new HashMap<>();
  private List<PantryItemDto> currentPantryList = new ArrayList<>();
  private List<RecipeMatchItemDto> lastMatchResults = new ArrayList<>();

  private final Handler handler = new Handler(Looper.getMainLooper());

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentDiscoverBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    demoDataManager = DemoDataManager.getInstance(requireContext());

    setupToggleSource();
    setupPantryList();
    setupDemoShortcut();
    setupPreferences();
    setupFindMealsAction();
    setupFilterChips();

    // Auto-select demo sample ingredients by default for presentation readiness
    applyDemoSampleIngredients();
  }

  private void setupToggleSource() {
    binding.toggleIngredientSource.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) {
            if (checkedId == R.id.tabMyPantry) {
              binding.containerMyPantry.setVisibility(View.VISIBLE);
              binding.containerAddIngredients.setVisibility(View.GONE);
            } else if (checkedId == R.id.tabAddIngredients) {
              binding.containerMyPantry.setVisibility(View.GONE);
              binding.containerAddIngredients.setVisibility(View.VISIBLE);
            }
          }
        });

    binding.btnAddCustomIngredient.setOnClickListener(
        v -> {
          String text =
              binding.etCustomIngredientSearch.getText() != null
                  ? binding.etCustomIngredientSearch.getText().toString().trim()
                  : "";
          if (!text.isEmpty()) {
            String fakeId = "food_" + text.toLowerCase(Locale.ROOT).replaceAll("\\s+", "_");
            selectedIngredientIds.add(fakeId);
            ingredientDisplayNames.put(fakeId, text);
            binding.etCustomIngredientSearch.setText("");
            renderSelectedChips();
            Toast.makeText(requireContext(), "Added " + text, Toast.LENGTH_SHORT).show();
          }
        });

    binding.btnClearSelected.setOnClickListener(
        v -> {
          selectedIngredientIds.clear();
          renderSelectedChips();
          renderPantryItems(
              binding.etFilterPantry.getText() != null
                  ? binding.etFilterPantry.getText().toString()
                  : "");
        });
  }

  private void setupDemoShortcut() {
    binding.btnTrySampleIngredients.setOnClickListener(
        v -> {
          applyDemoSampleIngredients();
          Toast.makeText(
                  requireContext(),
                  "Sample ingredients selected: Rice, Moong dal, Spinach, Tomato & Onion (2 Servings)",
                  Toast.LENGTH_SHORT)
              .show();
        });
  }

  private void applyDemoSampleIngredients() {
    selectedIngredientIds.clear();
    selectedIngredientIds.add("food_cooked_rice");
    selectedIngredientIds.add("food_moong_dal");
    selectedIngredientIds.add("food_spinach_palak");
    selectedIngredientIds.add("food_tomato");
    selectedIngredientIds.add("food_onion");

    binding.chipServings2.setChecked(true);
    binding.chipMealLunch.setChecked(true);

    renderSelectedChips();
    renderPantryItems(
        binding.etFilterPantry.getText() != null
            ? binding.etFilterPantry.getText().toString()
            : "");
    performIngredientMatch();
  }

  private void setupPantryList() {
    currentPantryList = demoDataManager.getPantryItems();
    for (PantryItemDto item : currentPantryList) {
      String display = item.foodNameEn;
      if (item.foodNameHi != null && !item.foodNameHi.isEmpty()) {
        display += " (" + item.foodNameHi + ")";
      }
      ingredientDisplayNames.put(item.foodId, display);
    }

    renderPantryItems("");

    binding.etFilterPantry.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            renderPantryItems(s != null ? s.toString() : "");
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    binding.btnSelectAllSuitable.setOnClickListener(
        v -> {
          for (PantryItemDto item : currentPantryList) {
            selectedIngredientIds.add(item.foodId);
          }
          renderSelectedChips();
          renderPantryItems(
              binding.etFilterPantry.getText() != null
                  ? binding.etFilterPantry.getText().toString()
                  : "");
        });
  }

  private void renderPantryItems(String filter) {
    binding.layoutPantryItemsList.removeAllViews();
    String query = filter != null ? filter.trim().toLowerCase(Locale.ROOT) : "";

    for (PantryItemDto item : currentPantryList) {
      if (!query.isEmpty()) {
        boolean match =
            (item.foodNameEn != null && item.foodNameEn.toLowerCase(Locale.ROOT).contains(query))
                || (item.foodNameHi != null && item.foodNameHi.contains(query));
        if (!match) continue;
      }

      MaterialCardView card = new MaterialCardView(requireContext());
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 4, 0, 4);
      card.setLayoutParams(params);
      card.setCardBackgroundColor(requireContext().getColor(R.color.brand_surface));
      card.setStrokeColor(requireContext().getColor(R.color.outline_subtle));
      card.setStrokeWidth(1);
      card.setRadius(12);

      LinearLayout row = new LinearLayout(requireContext());
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setGravity(android.view.Gravity.CENTER_VERTICAL);
      row.setPadding(12, 8, 12, 8);

      CheckBox cb = new CheckBox(requireContext());
      boolean isSelected = selectedIngredientIds.contains(item.foodId);
      cb.setChecked(isSelected);
      cb.setOnCheckedChangeListener(
          (buttonView, isChecked) -> {
            if (isChecked) {
              selectedIngredientIds.add(item.foodId);
            } else {
              selectedIngredientIds.remove(item.foodId);
            }
            renderSelectedChips();
          });
      row.addView(cb);

      LinearLayout textCol = new LinearLayout(requireContext());
      textCol.setOrientation(LinearLayout.VERTICAL);
      textCol.setLayoutParams(
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

      TextView name = new TextView(requireContext());
      name.setText(item.foodNameEn);
      name.setTextColor(requireContext().getColor(R.color.text_primary));
      name.setTextSize(14f);
      name.setTypeface(null, android.graphics.Typeface.BOLD);
      textCol.addView(name);

      TextView sub = new TextView(requireContext());
      String subText = (int) item.quantityG + " " + item.unit + " available";
      if (item.daysUntilExpiry <= 3) {
        subText += " • ⚠️ Expires in " + item.daysUntilExpiry + " days";
        sub.setTextColor(requireContext().getColor(R.color.status_warning));
      } else {
        sub.setTextColor(requireContext().getColor(R.color.text_secondary));
      }
      sub.setText(subText);
      sub.setTextSize(12f);
      textCol.addView(sub);

      row.addView(textCol);
      card.addView(row);
      binding.layoutPantryItemsList.addView(card);
    }
  }

  private void renderSelectedChips() {
    binding.chipGroupSelected.removeAllViews();
    int count = selectedIngredientIds.size();
    binding.tvSelectedCount.setText("Selected Ingredients (" + count + ")");
    binding.btnClearSelected.setVisibility(count > 0 ? View.VISIBLE : View.GONE);

    for (String id : selectedIngredientIds) {
      Chip chip = new Chip(requireContext());
      String name = ingredientDisplayNames.getOrDefault(id, id.replace("food_", ""));
      chip.setText(name);
      chip.setCloseIconVisible(true);
      chip.setChipBackgroundColorResource(R.color.brand_secondary);
      chip.setTextColor(requireContext().getColor(R.color.brand_primary));
      chip.setCloseIconTintResource(R.color.brand_primary);
      chip.setOnCloseIconClickListener(
          v -> {
            selectedIngredientIds.remove(id);
            renderSelectedChips();
            renderPantryItems(
                binding.etFilterPantry.getText() != null
                    ? binding.etFilterPantry.getText().toString()
                    : "");
          });
      binding.chipGroupSelected.addView(chip);
    }
  }

  private void setupPreferences() {
    // Default profile preferences are automatically pre-selected in XML
  }

  private int getSelectedServings() {
    if (binding.chipServings1.isChecked()) return 1;
    if (binding.chipServings3.isChecked()) return 3;
    if (binding.chipServings4.isChecked()) return 4;
    if (binding.chipServingsMore.isChecked()) return 5;
    return 2; // Default
  }

  private String getSelectedMealSlot() {
    if (binding.chipMealBreakfast.isChecked()) return "BREAKFAST";
    if (binding.chipMealLunch.isChecked()) return "LUNCH";
    if (binding.chipMealDinner.isChecked()) return "DINNER";
    if (binding.chipMealSnack.isChecked()) return "SNACK";
    return "ANY";
  }

  private String getSelectedPrepTime() {
    if (binding.chipTime15.isChecked()) return "< 15 min";
    if (binding.chipTime30.isChecked()) return "< 30 min";
    if (binding.chipTime45.isChecked()) return "< 45 min";
    return "";
  }

  private String getSelectedDiet() {
    if (binding.chipDietVegan.isChecked()) return "VEGAN";
    if (binding.chipDietJain.isChecked()) return "JAIN";
    if (binding.chipDietNoRestriction.isChecked()) return "ANY";
    return "VEGETARIAN";
  }

  private void setupFindMealsAction() {
    binding.btnFindWhatICanCook.setOnClickListener(
        v -> {
          if (selectedIngredientIds.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Please select ingredients or tap 'Try Sample' above",
                    Toast.LENGTH_SHORT)
                .show();
            return;
          }
          performIngredientMatch();
        });
  }

  private void performIngredientMatch() {
    binding.layoutLoadingMatch.setVisibility(View.VISIBLE);
    binding.layoutResultsCards.removeAllViews();
    binding.tvEmptyMessage.setVisibility(View.GONE);

    int servings = getSelectedServings();
    String mealSlot = getSelectedMealSlot();
    String prepTime = getSelectedPrepTime();
    String dietPref = getSelectedDiet();

    handler.postDelayed(
        () -> {
          if (!isAdded() || binding == null) return;
          binding.layoutLoadingMatch.setVisibility(View.GONE);

          repository.matchIngredients(
              new ArrayList<>(selectedIngredientIds),
              servings,
              mealSlot,
              prepTime,
              dietPref,
              new AppRepository.DataCallback<RecipeMatchResponseDto>() {
                @Override
                public void onSuccess(RecipeMatchResponseDto data) {
                  if (!isAdded() || binding == null) return;
                  lastMatchResults = data.matches != null ? data.matches : new ArrayList<>();
                  renderMatchResults(lastMatchResults);
                }

                @Override
                public void onError(String message) {
                  if (!isAdded() || binding == null) return;
                  binding.tvEmptyMessage.setVisibility(View.VISIBLE);
                  binding.tvEmptyMessage.setText("Error finding recipes: " + message);
                }
              });
        },
        350);
  }

  private void setupFilterChips() {
    binding.chipGroupResultsFilter.setOnCheckedChangeListener(
        (group, checkedId) -> {
          if (lastMatchResults.isEmpty()) return;
          List<RecipeMatchItemDto> sorted = new ArrayList<>(lastMatchResults);

          if (checkedId == R.id.filterQuickest) {
            sorted.sort(
                Comparator.comparingInt(
                    (RecipeMatchItemDto r) -> r.recipe.prepTimeMinutes + r.recipe.cookTimeMinutes));
          } else if (checkedId == R.id.filterMostAvailable) {
            sorted.sort((a, b) -> Integer.compare(b.matchPercentage, a.matchPercentage));
          } else if (checkedId == R.id.filterFewestMissing) {
            sorted.sort(
                Comparator.comparingInt(
                    (RecipeMatchItemDto r) ->
                        r.missingIngredients != null ? r.missingIngredients.size() : 0));
          } else if (checkedId == R.id.filterHighProtein) {
            sorted.sort((a, b) -> Double.compare(b.recipe.proteinG, a.recipe.proteinG));
          }

          renderMatchResults(sorted);
        });
  }

  private void renderMatchResults(List<RecipeMatchItemDto> matches) {
    binding.layoutResultsCards.removeAllViews();
    int count = matches != null ? matches.size() : 0;
    binding.tvMatchCountBadge.setText(count + " recipes found");

    if (count == 0) {
      binding.tvEmptyMessage.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmptyMessage.setVisibility(View.GONE);

    for (RecipeMatchItemDto item : matches) {
      if (item.recipe == null) continue;
      addRecipeResultCard(item);
    }
  }

  private void addRecipeResultCard(RecipeMatchItemDto item) {
    RecipeDetailDto recipe = item.recipe;

    MaterialCardView card = new MaterialCardView(requireContext());
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 8, 0, 8);
    card.setLayoutParams(params);
    card.setCardBackgroundColor(requireContext().getColor(R.color.brand_surface));
    card.setStrokeColor(requireContext().getColor(R.color.outline_subtle));
    card.setStrokeWidth(1);
    card.setRadius(16);
    card.setCardElevation(0);

    LinearLayout container = new LinearLayout(requireContext());
    container.setOrientation(LinearLayout.VERTICAL);
    container.setPadding(20, 16, 20, 16);

    // Header row: Recipe Name + Arrow
    LinearLayout headerRow = new LinearLayout(requireContext());
    headerRow.setOrientation(LinearLayout.HORIZONTAL);
    headerRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

    TextView title = new TextView(requireContext());
    title.setText(recipe.titleEn);
    title.setTextColor(requireContext().getColor(R.color.text_primary));
    title.setTextSize(17f);
    title.setTypeface(null, android.graphics.Typeface.BOLD);
    title.setLayoutParams(
        new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
    headerRow.addView(title);

    TextView arrow = new TextView(requireContext());
    arrow.setText("→");
    arrow.setTextColor(requireContext().getColor(R.color.brand_primary));
    arrow.setTextSize(18f);
    arrow.setTypeface(null, android.graphics.Typeface.BOLD);
    headerRow.addView(arrow);

    container.addView(headerRow);

    if (recipe.titleHi != null && !recipe.titleHi.isEmpty()) {
      TextView hiTitle = new TextView(requireContext());
      hiTitle.setText(recipe.titleHi);
      hiTitle.setTextColor(requireContext().getColor(R.color.brand_primary));
      hiTitle.setTextSize(13f);
      container.addView(hiTitle);
    }

    // Match Badge Pill
    LinearLayout badgeRow = new LinearLayout(requireContext());
    badgeRow.setOrientation(LinearLayout.HORIZONTAL);
    badgeRow.setPadding(0, 8, 0, 6);
    badgeRow.setGravity(android.view.Gravity.CENTER_VERTICAL);

    TextView matchBadge = new TextView(requireContext());
    matchBadge.setText(item.readinessBadge != null ? item.readinessBadge : "Ready to Cook");
    matchBadge.setBackgroundResource(R.drawable.ic_nav_discover);
    matchBadge.setBackgroundTintList(
        android.content.res.ColorStateList.valueOf(
            requireContext().getColor(R.color.brand_secondary)));
    matchBadge.setTextColor(requireContext().getColor(R.color.brand_primary));
    matchBadge.setTextSize(11f);
    matchBadge.setTypeface(null, android.graphics.Typeface.BOLD);
    matchBadge.setPadding(16, 6, 16, 6);
    badgeRow.addView(matchBadge);

    TextView matchScore = new TextView(requireContext());
    matchScore.setText(item.matchPercentage + "% Match");
    matchScore.setTextColor(requireContext().getColor(R.color.brand_primary_dark));
    matchScore.setTextSize(12f);
    matchScore.setTypeface(null, android.graphics.Typeface.BOLD);
    matchScore.setPadding(12, 0, 0, 0);
    badgeRow.addView(matchScore);

    container.addView(badgeRow);

    // Meta row: Time, Servings, Calories, Protein
    LinearLayout metaRow = new LinearLayout(requireContext());
    metaRow.setOrientation(LinearLayout.HORIZONTAL);
    metaRow.setPadding(0, 4, 0, 4);

    int totalMinutes = recipe.prepTimeMinutes + recipe.cookTimeMinutes;
    TextView metaTime = new TextView(requireContext());
    metaTime.setText("⏱ " + totalMinutes + " min  •  ");
    metaTime.setTextColor(requireContext().getColor(R.color.text_secondary));
    metaTime.setTextSize(12f);
    metaRow.addView(metaTime);

    TextView metaServings = new TextView(requireContext());
    metaServings.setText(recipe.defaultServings + " servings  •  ");
    metaServings.setTextColor(requireContext().getColor(R.color.text_secondary));
    metaServings.setTextSize(12f);
    metaRow.addView(metaServings);

    TextView metaCalories = new TextView(requireContext());
    metaCalories.setText((int) recipe.caloriesKcal + " kcal  •  ");
    metaCalories.setTextColor(requireContext().getColor(R.color.brand_primary));
    metaCalories.setTextSize(12f);
    metaCalories.setTypeface(null, android.graphics.Typeface.BOLD);
    metaRow.addView(metaCalories);

    TextView metaProtein = new TextView(requireContext());
    metaProtein.setText(String.format(Locale.US, "%.1fg protein", recipe.proteinG));
    metaProtein.setTextColor(requireContext().getColor(R.color.text_secondary));
    metaProtein.setTextSize(12f);
    metaRow.addView(metaProtein);

    container.addView(metaRow);

    // Missing ingredients summary if any
    if (item.missingIngredients != null && !item.missingIngredients.isEmpty()) {
      TextView missingText = new TextView(requireContext());
      missingText.setText("Missing: " + String.join(", ", item.missingIngredients));
      missingText.setTextColor(requireContext().getColor(R.color.status_warning));
      missingText.setTextSize(11f);
      missingText.setPadding(0, 4, 0, 0);
      container.addView(missingText);
    }

    card.addView(container);

    // Card Click -> Open RecipeDetailActivity with recipe and servings!
    card.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
          intent.putExtra("recipe_id", recipe.id);
          intent.putExtra("servings", recipe.defaultServings);
          startActivity(intent);
        });

    binding.layoutResultsCards.addView(card);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    handler.removeCallbacksAndMessages(null);
    binding = null;
  }
}
