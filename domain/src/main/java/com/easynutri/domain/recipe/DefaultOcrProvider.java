package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of OcrProvider, parsing optical character recognition text (from grocery
 * receipts, pantry labels, or packaging) using the domain IngredientResolver.
 */
public final class DefaultOcrProvider implements OcrProvider {

  private final IngredientResolver resolver;

  public DefaultOcrProvider(IngredientResolver resolver) {
    this.resolver = resolver != null ? resolver : new DefaultIngredientResolver();
  }

  public DefaultOcrProvider() {
    this(new DefaultIngredientResolver());
  }

  @Override
  public List<IngredientCandidate> extractIngredients(String ocrText) {
    Objects.requireNonNull(ocrText, "ocrText cannot be null");
    if (ocrText.isBlank()) {
      return List.of();
    }

    return resolver.resolveAll(ocrText);
  }
}
