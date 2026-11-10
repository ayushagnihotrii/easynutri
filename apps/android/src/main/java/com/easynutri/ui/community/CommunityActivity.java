package com.easynutri.ui.community;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.easynutri.R;
import com.easynutri.data.model.Models.CommunityRecipeDto;
import com.easynutri.data.repository.AppRepository;
import com.easynutri.databinding.ActivityCommunityBinding;
import com.google.android.material.card.MaterialCardView;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

@AndroidEntryPoint
public class CommunityActivity extends AppCompatActivity {

  @Inject AppRepository repository;

  private ActivityCommunityBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityCommunityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setupToolbar();
    setupActions();
    loadFeed();
  }

  private void setupToolbar() {
    binding.toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void setupActions() {
    binding.btnShareRecipe.setOnClickListener(v -> showShareRecipeDialog());
  }

  private void loadFeed() {
    binding.progressBar.setVisibility(View.VISIBLE);
    binding.tvEmpty.setVisibility(View.GONE);

    repository.getCommunityFeed(
        "recent",
        null,
        new AppRepository.DataCallback<>() {
          @Override
          public void onSuccess(List<CommunityRecipeDto> recipes) {
            binding.progressBar.setVisibility(View.GONE);
            renderRecipes(recipes);
          }

          @Override
          public void onError(String message) {
            binding.progressBar.setVisibility(View.GONE);
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvEmpty.setText("No community posts available currently.");
          }
        });
  }

  private void renderRecipes(List<CommunityRecipeDto> recipes) {
    binding.layoutCommunityRecipes.removeAllViews();
    if (recipes == null || recipes.isEmpty()) {
      binding.tvEmpty.setVisibility(View.VISIBLE);
      return;
    }
    binding.tvEmpty.setVisibility(View.GONE);

    for (CommunityRecipeDto recipe : recipes) {
      MaterialCardView card = new MaterialCardView(this);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
      params.setMargins(0, 8, 0, 8);
      card.setLayoutParams(params);
      card.setCardBackgroundColor(getColor(R.color.brand_surface));
      card.setStrokeColor(getColor(R.color.outline_subtle));
      card.setStrokeWidth(2);
      card.setRadius(16);

      LinearLayout container = new LinearLayout(this);
      container.setOrientation(LinearLayout.VERTICAL);
      container.setPadding(24, 20, 24, 20);

      TextView author = new TextView(this);
      author.setText(
          "Posted by @"
              + (recipe.authorUsername != null ? recipe.authorUsername : "community_cook"));
      author.setTextColor(getColor(R.color.brand_primary));
      author.setTextSize(12);

      TextView title = new TextView(this);
      title.setText(recipe.title);
      title.setTextColor(getColor(R.color.text_primary));
      title.setTextSize(16);
      title.setTypeface(null, android.graphics.Typeface.BOLD);
      title.setPadding(0, 4, 0, 4);

      TextView desc = new TextView(this);
      desc.setText(
          recipe.description != null
              ? recipe.description
              : "Nutritious community recipe approved by SafetyGate.");
      desc.setTextColor(getColor(R.color.text_secondary));
      desc.setTextSize(13);

      LinearLayout footer = new LinearLayout(this);
      footer.setOrientation(LinearLayout.HORIZONTAL);
      footer.setPadding(0, 12, 0, 0);
      footer.setGravity(android.view.Gravity.CENTER_VERTICAL);

      Button btnLike =
          new Button(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
      btnLike.setText("❤️ " + recipe.likeCount);
      btnLike.setTextSize(11);
      btnLike.setOnClickListener(
          v -> {
            repository.toggleLikeCommunityRecipe(
                recipe.id,
                new AppRepository.DataCallback<Boolean>() {
                  @Override
                  public void onSuccess(Boolean resp) {
                    recipe.likeCount++;
                    btnLike.setText("❤️ " + recipe.likeCount);
                    Toast.makeText(CommunityActivity.this, "Liked recipe!", Toast.LENGTH_SHORT)
                        .show();
                  }

                  @Override
                  public void onError(String message) {
                    Toast.makeText(CommunityActivity.this, "Like: " + message, Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          });
      footer.addView(btnLike);

      TextView commentCount = new TextView(this);
      commentCount.setText("   💬 " + recipe.commentCount + " comments");
      commentCount.setTextColor(getColor(R.color.text_secondary));
      commentCount.setTextSize(12);
      footer.addView(commentCount);

      container.addView(author);
      container.addView(title);
      container.addView(desc);
      container.addView(footer);

      card.addView(container);
      binding.layoutCommunityRecipes.addView(card);
    }
  }

  private void showShareRecipeDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Share Community Recipe");

    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setPadding(40, 20, 40, 20);

    EditText etTitle = new EditText(this);
    etTitle.setHint("Recipe Title (e.g. Spiced Moong Dal Khichdi)");
    layout.addView(etTitle);

    EditText etDesc = new EditText(this);
    etDesc.setHint("Short description & health benefits");
    layout.addView(etDesc);

    EditText etInstructions = new EditText(this);
    etInstructions.setHint("Instructions (step by step)");
    layout.addView(etInstructions);

    builder.setView(layout);

    builder.setPositiveButton(
        "Publish",
        (dialog, which) -> {
          String title = etTitle.getText().toString().trim();
          String desc = etDesc.getText().toString().trim();
          String inst = etInstructions.getText().toString().trim();

          if (title.isEmpty()) {
            Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
          }

          repository.createCommunityRecipe(
              title,
              desc,
              350,
              Collections.<String>emptyList(),
              Collections.singletonList(inst),
              new AppRepository.DataCallback<CommunityRecipeDto>() {
                @Override
                public void onSuccess(CommunityRecipeDto data) {
                  Toast.makeText(
                          CommunityActivity.this,
                          "Recipe submitted for safety review & published!",
                          Toast.LENGTH_LONG)
                      .show();
                  loadFeed();
                }

                @Override
                public void onError(String message) {
                  Toast.makeText(
                          CommunityActivity.this, "Publish error: " + message, Toast.LENGTH_SHORT)
                      .show();
                }
              });
        });

    builder.setNegativeButton(R.string.btn_cancel, null);
    builder.show();
  }
}
