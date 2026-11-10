package com.easynutri.domain.moderation;

import java.util.List;
import java.util.Objects;

/** Immutable outcome of automated content screening with clinical reasoning. */
public record ScreeningResult(
    ScreeningVerdict verdict,
    String flaggedCategory,
    List<String> reasons,
    boolean requiresHumanReview) {

  public ScreeningResult {
    Objects.requireNonNull(verdict, "verdict cannot be null");
    reasons = List.copyOf(reasons != null ? reasons : List.of());
  }

  public static ScreeningResult approve() {
    return new ScreeningResult(ScreeningVerdict.APPROVE, null, List.of(), false);
  }

  public static ScreeningResult reviewRequired(String category, String reason) {
    return new ScreeningResult(ScreeningVerdict.REVIEW_REQUIRED, category, List.of(reason), true);
  }

  public static ScreeningResult block(String category, String reason) {
    return new ScreeningResult(ScreeningVerdict.BLOCK, category, List.of(reason), true);
  }
}
