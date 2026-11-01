package com.easynutri.domain.rules;

import java.util.List;
import java.util.Objects;

/** Result of evaluating a domain candidate against user profile constraints. */
public record SafetyResult(RuleVerdict verdict, List<String> reasons, List<String> suggestedSwaps) {
  public SafetyResult {
    Objects.requireNonNull(verdict, "verdict cannot be null");
    reasons = List.copyOf(reasons != null ? reasons : List.of());
    suggestedSwaps = List.copyOf(suggestedSwaps != null ? suggestedSwaps : List.of());
  }

  public boolean isSafe() {
    return verdict != RuleVerdict.BLOCK;
  }

  public static SafetyResult allow() {
    return new SafetyResult(RuleVerdict.ALLOW, List.of(), List.of());
  }

  public static SafetyResult block(String reason) {
    return new SafetyResult(RuleVerdict.BLOCK, List.of(reason), List.of());
  }

  public static SafetyResult warn(String reason) {
    return new SafetyResult(RuleVerdict.WARN, List.of(reason), List.of());
  }
}
