package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.List;

/** Domain provider contract for recognizing food items and ingredients from images. */
public interface FoodRecognitionProvider {

  /**
   * Analyzes an image payload and returns detected ingredient candidates.
   *
   * @param imageBytes raw image bytes
   * @param contentType MIME type (e.g. "image/jpeg", "image/png")
   * @return recognized ingredient candidates
   */
  List<IngredientCandidate> recognizeImage(byte[] imageBytes, String contentType);
}
