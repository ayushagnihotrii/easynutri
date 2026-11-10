package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.List;

/**
 * Domain provider contract for extracting ingredient candidates from OCR text (packaging/receipts).
 */
public interface OcrProvider {

  /**
   * Parses raw OCR string and resolves all recognized ingredients.
   *
   * @param ocrText raw optical character recognition string
   * @return extracted ingredient candidates
   */
  List<IngredientCandidate> extractIngredients(String ocrText);
}
