package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.easynutri.domain.recipe.LeftoverSafetyEvaluator;
import com.easynutri.domain.rules.RuleVerdict;
import com.easynutri.domain.rules.SafetyResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LeftoverSafetyEvaluatorTest {

  @Test
  @DisplayName("Room temperature food exceeds 4 hours triggers BLOCK")
  void testRoomTemperatureLimits() {
    SafetyResult safe =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Cooked Rice", 1, "ROOM_TEMP");
    assertEquals(RuleVerdict.ALLOW, safe.verdict());

    SafetyResult warning =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Cooked Rice", 3, "ROOM_TEMP");
    assertEquals(RuleVerdict.WARN, warning.verdict());

    SafetyResult blocked =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Cooked Rice", 5, "ROOM_TEMP");
    assertEquals(RuleVerdict.BLOCK, blocked.verdict());
    assertTrue(blocked.reasons().get(0).contains("exceeds safe bacterial boundaries"));
  }

  @Test
  @DisplayName("Refrigerated food limits: >72 hours triggers BLOCK")
  void testRefrigeratedLimits() {
    SafetyResult safe =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Dal Tadka", 24, "REFRIGERATED");
    assertEquals(RuleVerdict.ALLOW, safe.verdict());

    SafetyResult warning =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Dal Tadka", 50, "REFRIGERATED");
    assertEquals(RuleVerdict.WARN, warning.verdict());

    SafetyResult blocked =
        LeftoverSafetyEvaluator.evaluateLeftoverSafety("Dal Tadka", 75, "REFRIGERATED");
    assertEquals(RuleVerdict.BLOCK, blocked.verdict());
    assertTrue(blocked.reasons().get(0).contains("Storage Limit Exceeded"));
  }
}
