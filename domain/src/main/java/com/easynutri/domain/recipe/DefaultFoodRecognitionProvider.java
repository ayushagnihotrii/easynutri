package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of FoodRecognitionProvider. Provides resilient, honest fallback for visual
 * recognition without hallucinating or fabricating confidence scores when external cloud vision
 * endpoints are unconfigured.
 */
public final class DefaultFoodRecognitionProvider implements FoodRecognitionProvider {

  private final IngredientResolver resolver;

  public DefaultFoodRecognitionProvider(IngredientResolver resolver) {
    this.resolver = resolver != null ? resolver : new DefaultIngredientResolver();
  }

  public DefaultFoodRecognitionProvider() {
    this(new DefaultIngredientResolver());
  }

  @Override
  public List<IngredientCandidate> recognizeImage(byte[] imageBytes, String contentType) {
    Objects.requireNonNull(imageBytes, "imageBytes cannot be null");
    if (imageBytes.length == 0) {
      return List.of();
    }

    // In local standalone mode without an external GPU vision pipeline or API key,
    // we return an assisted candidate inviting user confirmation rather than hallucinating
    List<IngredientCandidate> fallback = new ArrayList<>();
    fallback.add(
        new IngredientCandidate(
            "Captured Photo Item",
            "food_unknown",
            "Photo Food Item (Confirm name)",
            "",
            "GENERAL",
            100.0,
            "g",
            100.0,
            false,
            0,
            "FRESH"));
    return List.copyOf(fallback);
  }
}
