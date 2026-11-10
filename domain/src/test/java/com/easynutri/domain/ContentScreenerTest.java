package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.moderation.ContentScreener;
import com.easynutri.domain.moderation.ScreeningResult;
import com.easynutri.domain.moderation.ScreeningVerdict;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ContentScreenerTest {

  @Test
  @DisplayName("Approves safe wholesome community recipe text")
  void testApproveSafeRecipe() {
    ScreeningResult result =
        ContentScreener.screen(
            "Spiced Red Lentil Dal with Wilted Spinach",
            "A nourishing, high-fiber Indian lentil soup rich in plant protein and iron.");

    assertEquals(ScreeningVerdict.APPROVE, result.verdict());
    assertFalse(result.requiresHumanReview());
  }

  @Test
  @DisplayName("Blocks dangerous medical claims claiming to cure chronic illness or replace drugs")
  void testBlockDangerousMedicalClaim() {
    ScreeningResult result =
        ContentScreener.screen(
            "Miracle Karela Smoothie",
            "Drink this bitter gourd juice every morning! It cures diabetes and allows you to stop taking insulin.");

    assertEquals(ScreeningVerdict.BLOCK, result.verdict());
    assertEquals("DANGEROUS_MEDICAL_CLAIM", result.flaggedCategory());
    assertTrue(result.requiresHumanReview());
  }

  @Test
  @DisplayName("Blocks starvation and eating disorder encouragement")
  void testBlockEatingDisorderContent() {
    ScreeningResult result =
        ContentScreener.screen(
            "Extreme Rapid Drop Diet",
            "How to eat under 500 calories and starve yourself to lose 10kg in 3 days.");

    assertEquals(ScreeningVerdict.BLOCK, result.verdict());
    assertEquals("EATING_DISORDER_PROMOTION", result.flaggedCategory());
  }

  @Test
  @DisplayName("Routes excessive dosage claims to clinical human review queue")
  void testRouteHighRiskClaimToReview() {
    ScreeningResult result =
        ContentScreener.screen(
            "High Dose Protocol", "Take 10000 IU daily and megadose supplements for fast healing.");

    assertEquals(ScreeningVerdict.REVIEW_REQUIRED, result.verdict());
    assertEquals("HIGH_RISK_CLAIM", result.flaggedCategory());
    assertTrue(result.requiresHumanReview());
  }
}
