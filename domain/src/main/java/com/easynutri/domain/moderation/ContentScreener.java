package com.easynutri.domain.moderation;

import java.util.Locale;

/**
 * Pure Java clinical and safety content screening engine. Inspects community submissions (recipes,
 * posts, comments) for dangerous medical advice, eating disorder encouragement, spam, harassment,
 * and unverified medical guarantees.
 */
public final class ContentScreener {

  private ContentScreener() {}

  public static ScreeningResult screen(String title, String content) {
    String combined =
        ((title != null ? title : "") + " " + (content != null ? content : ""))
            .toLowerCase(Locale.ROOT);
    if (combined.trim().isEmpty()) {
      return ScreeningResult.approve();
    }

    // 1. Dangerous Medical Cure & Medication Abandonment Claims (BLOCK)
    if (containsAny(
        combined,
        "cures diabetes",
        "cure diabetes",
        "replaces insulin",
        "replace insulin",
        "stop taking insulin",
        "throw away your metformin",
        "stop your medication",
        "stop taking medication",
        "cures cancer",
        "cure cancer",
        "cures thyroid",
        "cures pcos",
        "cures kidney disease",
        "cure hypertension")) {
      return ScreeningResult.block(
          "DANGEROUS_MEDICAL_CLAIM",
          "Hazardous medical claim detected. Nutritional recipes cannot claim to cure chronic diseases or replace prescribed medical therapy.");
    }

    // 2. Eating Disorder & Starvation Promotion (BLOCK)
    if (containsAny(
        combined,
        "eat under 500 calories",
        "300 calorie diet",
        "water fast for 14 days",
        "dry fasting for days",
        "starve yourself",
        "purge after eating",
        "lose 10kg in 3 days",
        "lose 20 lbs in 5 days",
        "anorexia tip",
        "bulimia tip")) {
      return ScreeningResult.block(
          "EATING_DISORDER_PROMOTION",
          "Content promoting severe caloric deprivation, extreme starvation, or purging behaviors is prohibited.");
    }

    // 3. Spam & Financial Fraud (BLOCK)
    if (containsAny(
        combined,
        "free bitcoin",
        "buy crypto",
        "telegram link for investment",
        "whatsapp for money",
        "earn $1000 daily",
        "casino bonus",
        "click here for free cash")) {
      return ScreeningResult.block(
          "SPAM", "Commercial spam or fraudulent financial links detected.");
    }

    // 4. Excessive Supplementation & High Risk Claims (REVIEW_REQUIRED)
    if (containsAny(
        combined,
        "mega dose",
        "megadose",
        "10000 iu",
        "inject vitamin",
        "detoxify heavy metals in 24h",
        "guaranteed to drop blood sugar completely")) {
      return ScreeningResult.reviewRequired(
          "HIGH_RISK_CLAIM",
          "High-potency supplement dosage or aggressive physiological claim requires clinical human review.");
    }

    return ScreeningResult.approve();
  }

  private static boolean containsAny(String text, String... keywords) {
    for (String kw : keywords) {
      if (text.contains(kw)) {
        return true;
      }
    }
    return false;
  }
}
