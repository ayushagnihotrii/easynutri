package com.easynutri.domain.recipe;

import com.easynutri.domain.model.IngredientCandidate;
import java.util.List;

/**
 * Domain contract for resolving unstructured user input (English, Hindi, Hinglish, aliases, voice
 * transcripts, OCR strings) into canonical IngredientCandidate records.
 */
public interface IngredientResolver {

  /**
   * Resolves a single ingredient input string.
   *
   * @param input raw input string (e.g. "2 katori chawal", "tomato", "pyaz", "500g paneer")
   * @return resolved IngredientCandidate
   */
  IngredientCandidate resolve(String input);

  /**
   * Resolves multiple ingredients from a compound natural language phrase or voice transcript.
   * (e.g. "I have two onions, three tomatoes and some paneer")
   *
   * @param text full compound natural language text
   * @return list of extracted IngredientCandidate records
   */
  List<IngredientCandidate> resolveAll(String text);
}
