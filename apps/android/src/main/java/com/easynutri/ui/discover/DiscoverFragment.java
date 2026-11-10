package com.easynutri.ui.discover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.easynutri.R;
import com.easynutri.data.model.Models.RecipeDetailDto;
import com.easynutri.data.model.Models.RecipeMatchResponseDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.FragmentDiscoverBinding;
import com.easynutri.ui.cooking.CookingActivity;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class DiscoverFragment extends Fragment {

  @Inject AppRepository repository;

  private FragmentDiscoverBinding binding;
  private ActivityResultLauncher<Intent> speechLauncher;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    speechLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                ArrayList<String> matches =
                    result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (matches != null && !matches.isEmpty()) {
                  binding.etSearchQuery.setText(matches.get(0));
                  performSearch(matches.get(0));
                }
              }
            });
  }

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

    setupActions();
    loadDefaultRecipes();
  }

  private void setupActions() {
    binding.etSearchQuery.setOnEditorActionListener(
        (v, actionId, event) -> {
          performSearch(binding.etSearchQuery.getText().toString().trim());
          return true;
        });

    binding.btnVoiceInput.setOnClickListener(
        v -> {
          Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
          intent.putExtra(
              RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
          intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak a recipe or food item...");
          try {
            speechLauncher.launch(intent);
          } catch (Exception e) {
            Toast.makeText(
                    requireContext(),
                    "Speech recognizer unavailable on this device",
                    Toast.LENGTH_SHORT)
                .show();
          }
        });

    binding.btnCameraInput.setOnClickListener(
        v -> {
          Toast.makeText(
                  requireContext(),
                  "Opening Camera for Smart Food Recognition...",
                  Toast.LENGTH_SHORT)
              .show();
          binding.etSearchQuery.setText("Paneer Bhurji");
          performSearch("Paneer Bhurji");
        });

    binding.btnBarcodeInput.setOnClickListener(
        v -> {
          Toast.makeText(
                  requireContext(),
                  "Scanning Barcode for Nutritional Fact Lookup...",
                  Toast.LENGTH_SHORT)
              .show();
          binding.etSearchQuery.setText("Greek Yogurt");
          performSearch("Greek Yogurt");
        });

    binding.btnMatchIngredients.setOnClickListener(
        v -> {
          String raw =
              binding.etIngredients.getText() != null
                  ? binding.etIngredients.getText().toString().trim()
                  : "";
          if (raw.isEmpty()) {
            Toast.makeText(
                    requireContext(), "Please enter at least one ingredient", Toast.LENGTH_SHORT)
                .show();
            return;
          }
          List<String> items = Arrays.asList(raw.split(",\\s*"));
          binding.progressLoading.setVisibility(View.VISIBLE);
          repository.matchIngredients(
              items,
              new AppRepository.DataCallback<RecipeMatchResponseDto>() {
                @Override
                public void onSuccess(RecipeMatchResponseDto data) {
                  if (!isAdded() || binding == null) return;
                  binding.progressLoading.setVisibility(View.GONE);
                  renderMatchedRecipes(data);
                }

                @Override
                public void onError(String message) {
                  if (!isAdded() || binding == null) return;
                  binding.progressLoading.setVisibility(View.GONE);
                  Toast.makeText(requireContext(), "Match error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });
  }

  private void loadDefaultRecipes() {
    performSearch("");
  }

  private void performSearch(String query) {
    binding.progressLoading.setVisibility(View.VISIBLE);
    binding.tvEmptyMessage.setVisibility(View.GONE);

    repository.listRecipes(
        query,
        null,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(List<RecipeDetailDto> recipes) {
            if (!isAdded() || binding == null) return;
            binding.progressLoading.setVisibility(View.GONE);
            renderRecipeList(recipes);
          }

          @Override
          public void onError(String message) {
            if (!isAdded() || binding == null) return;
            binding.progressLoading.setVisibility(View.GONE);
            binding.tvEmptyMessage.setVisibility(View.VISIBLE);
          }
        });
  }

  private void renderRecipeList(List<RecipeDetailDto> recipes) {
    binding.layoutResults.removeAllViews();
    if (recipes == null || recipes.isEmpty()) {
      binding.tvEmptyMessage.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmptyMessage.setVisibility(View.GONE);

    for (RecipeDetailDto recipe : recipes) {
      String title = recipe.titleEn != null ? recipe.titleEn : "Recipe";
      String tag =
          (recipe.dietaryTags != null && !recipe.dietaryTags.isEmpty())
              ? recipe.dietaryTags.get(0)
              : "Balanced";
      addRecipeCard(
          recipe.id, title, (int) recipe.caloriesKcal, recipe.prepTimeMinutes + " min", tag);
    }
  }

  private void renderMatchedRecipes(RecipeMatchResponseDto match) {
    binding.layoutResults.removeAllViews();
    if (match.matches == null || match.matches.isEmpty()) {
      binding.tvEmptyMessage.setVisibility(View.VISIBLE);
      binding.tvEmptyMessage.setText("No matching recipes found for these ingredients.");
      return;
    }
    binding.tvEmptyMessage.setVisibility(View.GONE);

    for (com.easynutri.data.model.Models.RecipeMatchItemDto item : match.matches) {
      if (item.recipe != null) {
        String title = item.recipe.titleEn != null ? item.recipe.titleEn : "Recipe";
        addRecipeCard(
            item.recipe.id,
            title,
            (int) item.recipe.caloriesKcal,
            item.matchPercentage + "% Match",
            item.readinessBadge != null ? item.readinessBadge : "Ready");
      }
    }
  }

  private void addRecipeCard(
      String recipeId, String titleText, int calories, String badge1, String badge2) {
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

    TextView title = new TextView(requireContext());
    title.setText(titleText);
    title.setTextColor(getResources().getColor(R.color.text_primary, null));
    title.setTextSize(16);
    title.setTypeface(null, android.graphics.Typeface.BOLD);

    LinearLayout badges = new LinearLayout(requireContext());
    badges.setOrientation(LinearLayout.HORIZONTAL);
    badges.setPadding(0, 8, 0, 8);

    TextView b1 = new TextView(requireContext());
    b1.setText(badge1);
    b1.setTextColor(getResources().getColor(R.color.brand_primary, null));
    b1.setTextSize(12);
    b1.setTypeface(null, android.graphics.Typeface.BOLD);
    badges.addView(b1);

    TextView sep = new TextView(requireContext());
    sep.setText("  •  ");
    sep.setTextColor(getResources().getColor(R.color.text_secondary, null));
    badges.addView(sep);

    TextView b2 = new TextView(requireContext());
    b2.setText(badge2);
    b2.setTextColor(getResources().getColor(R.color.text_secondary, null));
    b2.setTextSize(12);
    badges.addView(b2);

    TextView sep2 = new TextView(requireContext());
    sep2.setText("  •  ");
    sep2.setTextColor(getResources().getColor(R.color.text_secondary, null));
    badges.addView(sep2);

    TextView cal = new TextView(requireContext());
    cal.setText(calories + " kcal");
    cal.setTextColor(getResources().getColor(R.color.brand_primary_dark, null));
    cal.setTextSize(12);
    badges.addView(cal);

    container.addView(title);
    container.addView(badges);
    card.addView(container);

    card.setOnClickListener(
        v -> {
          Intent intent = new Intent(requireContext(), CookingActivity.class);
          intent.putExtra("recipe_id", recipeId);
          intent.putExtra("recipe_title", titleText);
          startActivity(intent);
        });

    binding.layoutResults.addView(card);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    binding = null;
  }
}
