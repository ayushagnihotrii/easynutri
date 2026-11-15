package com.easynutri.ui.recipe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.demo.DemoDataManager;
import com.easynutri.data.model.Models.RecipeDetailDto;
import com.easynutri.data.model.Models.ScaledIngredientDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityRecipeDetailBinding;
import com.easynutri.ui.cooking.CookingActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import javax.inject.Inject;

@AndroidEntryPoint
public class RecipeDetailActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityRecipeDetailBinding binding;
  private DemoDataManager demoDataManager;
  private RecipeDetailDto currentRecipe;
  private int currentServings = 2;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityRecipeDetailBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    demoDataManager = DemoDataManager.getInstance(this);

    setupToolbar();
    loadRecipeData();
    setupServingScaler();
    setupActions();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void loadRecipeData() {
    String recipeId = getIntent().getStringExtra("recipe_id");
    if (recipeId == null || recipeId.isEmpty()) {
      recipeId = "recipe_moong_khichdi";
    }
    currentServings = getIntent().getIntExtra("servings", 2);
    if (currentServings <= 0) currentServings = 2;

    currentRecipe = demoDataManager.getRecipeById(recipeId, currentServings);
    if (currentRecipe == null) {
      currentRecipe = demoDataManager.getAllRecipes().get(0);
    }

    renderRecipe();
  }

  private void setupServingScaler() {
    binding.btnDecreaseServings.setOnClickListener(
        v -> {
          if (currentServings > 1) {
            currentServings--;
            recalculateAndRender();
          }
        });

    binding.btnIncreaseServings.setOnClickListener(
        v -> {
          if (currentServings < 10) {
            currentServings++;
            recalculateAndRender();
          }
        });
  }

  private void recalculateAndRender() {
    if (currentRecipe == null) return;
    currentRecipe = demoDataManager.scaleRecipe(currentRecipe, currentServings);
    renderRecipe();
    Toast.makeText(
            this,
            "Scaled to " + currentServings + " servings with recalculated nutrition",
            Toast.LENGTH_SHORT)
        .show();
  }

  private void renderRecipe() {
    if (currentRecipe == null) return;

    binding.tvRecipeTitle.setText(currentRecipe.titleEn);
    binding.tvRecipeTitleHi.setText(currentRecipe.titleHi != null ? currentRecipe.titleHi : "");
    binding.tvRecipeDescription.setText(currentRecipe.description);

    int totalMinutes = currentRecipe.prepTimeMinutes + currentRecipe.cookTimeMinutes;
    binding.tvTotalTime.setText("⏱ " + totalMinutes + " min total");
    binding.tvPrepCookTime.setText(
        "Prep: "
            + currentRecipe.prepTimeMinutes
            + "m • Cook: "
            + currentRecipe.cookTimeMinutes
            + "m");

    binding.tvServingsCount.setText(String.valueOf(currentServings));

    // Clinical Nutrition Values
    binding.tvCalories.setText((int) currentRecipe.caloriesKcal + " kcal");
    binding.tvProtein.setText(String.format(java.util.Locale.US, "%.1fg", currentRecipe.proteinG));
    binding.tvCarbs.setText(String.format(java.util.Locale.US, "%.1fg", currentRecipe.carbsG));
    binding.tvFat.setText(String.format(java.util.Locale.US, "%.1fg", currentRecipe.fatG));

    // Dietary Tags
    binding.chipGroupTags.removeAllViews();
    if (currentRecipe.dietaryTags != null) {
      for (String tag : currentRecipe.dietaryTags) {
        Chip chip = new Chip(this);
        chip.setText(tag.replace("_", " ").toUpperCase(java.util.Locale.ROOT));
        chip.setChipBackgroundColorResource(R.color.brand_secondary);
        chip.setTextColor(getColor(R.color.brand_primary));
        chip.setTextSize(11f);
        chip.setEnsureMinTouchTargetSize(false);
        binding.chipGroupTags.addView(chip);
      }
    }

    // Render Scaled Ingredients
    renderIngredients();

    // Render Steps
    renderInstructions();
  }

  private void renderIngredients() {
    binding.layoutIngredientsContainer.removeAllViews();
    if (currentRecipe.ingredients == null) return;

    for (ScaledIngredientDto ing : currentRecipe.ingredients) {
      MaterialCardView card = new MaterialCardView(this);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 6, 0, 6);
      card.setLayoutParams(params);
      card.setCardBackgroundColor(getColor(R.color.brand_surface));
      card.setStrokeColor(getColor(R.color.outline_subtle));
      card.setStrokeWidth(1);
      card.setRadius(12);

      LinearLayout row = new LinearLayout(this);
      row.setOrientation(LinearLayout.HORIZONTAL);
      row.setPadding(16, 12, 16, 12);
      row.setGravity(android.view.Gravity.CENTER_VERTICAL);

      TextView dot = new TextView(this);
      dot.setText("•  ");
      dot.setTextColor(getColor(R.color.brand_primary));
      dot.setTextSize(16f);
      row.addView(dot);

      TextView name = new TextView(this);
      name.setLayoutParams(
          new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
      String displayName = ing.nameEn != null ? ing.nameEn : ing.foodId;
      if (ing.nameHi != null && !ing.nameHi.isEmpty()) {
        displayName += " (" + ing.nameHi + ")";
      }
      name.setText(displayName);
      name.setTextColor(getColor(R.color.text_primary));
      name.setTextSize(14f);
      row.addView(name);

      TextView amount = new TextView(this);
      amount.setText((int) ing.amountG + "g");
      amount.setTextColor(getColor(R.color.brand_primary_dark));
      amount.setTextSize(14f);
      amount.setTypeface(null, android.graphics.Typeface.BOLD);
      row.addView(amount);

      card.addView(row);
      binding.layoutIngredientsContainer.addView(card);
    }
  }

  private void renderInstructions() {
    binding.layoutInstructionsContainer.removeAllViews();
    if (currentRecipe.instructions == null) return;

    int stepNum = 1;
    for (String stepText : currentRecipe.instructions) {
      MaterialCardView card = new MaterialCardView(this);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 6, 0, 6);
      card.setLayoutParams(params);
      card.setCardBackgroundColor(getColor(R.color.brand_surface));
      card.setStrokeColor(getColor(R.color.outline_subtle));
      card.setStrokeWidth(1);
      card.setRadius(12);

      LinearLayout col = new LinearLayout(this);
      col.setOrientation(LinearLayout.VERTICAL);
      col.setPadding(16, 12, 16, 12);

      TextView header = new TextView(this);
      header.setText("Step " + stepNum++);
      header.setTextColor(getColor(R.color.brand_primary));
      header.setTextSize(13f);
      header.setTypeface(null, android.graphics.Typeface.BOLD);
      col.addView(header);

      TextView body = new TextView(this);
      body.setText(stepText.replaceFirst("^\\d+\\.\\s*", ""));
      body.setTextColor(getColor(R.color.text_primary));
      body.setTextSize(14f);
      body.setLineSpacing(4f, 1f);
      body.setPadding(0, 4, 0, 0);
      col.addView(body);

      card.addView(col);
      binding.layoutInstructionsContainer.addView(card);
    }
  }

  private void setupActions() {
    binding.btnStartCooking.setOnClickListener(
        v -> {
          Intent intent = new Intent(this, CookingActivity.class);
          intent.putExtra("recipe_id", currentRecipe.id);
          intent.putExtra("recipe_title", currentRecipe.titleEn);
          intent.putExtra("servings", currentServings);
          if (currentRecipe.instructions != null) {
            intent.putStringArrayListExtra("steps", new ArrayList<>(currentRecipe.instructions));
          }
          if (currentRecipe.ingredients != null) {
            ArrayList<String> ingsList = new ArrayList<>();
            for (ScaledIngredientDto ing : currentRecipe.ingredients) {
              ingsList.add(ing.nameEn + " - " + (int) ing.amountG + "g");
            }
            intent.putStringArrayListExtra("ingredients", ingsList);
          }
          startActivity(intent);
        });

    binding.btnLogMeal.setOnClickListener(
        v -> {
          demoDataManager.logMealToDiary(currentRecipe.id, currentServings, currentRecipe.mealSlot);
          Toast.makeText(
                  this,
                  "Logged "
                      + currentRecipe.titleEn
                      + " ("
                      + currentServings
                      + " servings) to Diary! 🌿",
                  Toast.LENGTH_LONG)
              .show();
        });

    binding.btnAddToMealPlan.setOnClickListener(
        v -> {
          Toast.makeText(
                  this,
                  "Added " + currentRecipe.titleEn + " to today's Meal Plan!",
                  Toast.LENGTH_SHORT)
              .show();
        });

    binding.btnSaveRecipe.setOnClickListener(
        v -> {
          Toast.makeText(
                  this,
                  "Added " + currentRecipe.titleEn + " to Saved Recipes ❤️",
                  Toast.LENGTH_SHORT)
              .show();
        });
  }
}
