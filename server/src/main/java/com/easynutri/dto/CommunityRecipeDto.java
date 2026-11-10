package com.easynutri.dto;

import java.time.Instant;
import java.util.List;

public record CommunityRecipeDto(
    Long id,
    Long authorId,
    String authorUsername,
    String authorFullName,
    String authorAvatar,
    boolean isAuthorPro,
    String authorProCategory,
    String title,
    String description,
    String coverImageUrl,
    int baseServings,
    int prepMinutes,
    int cookMinutes,
    String mealSlot,
    List<String> dietaryTags,
    String instructionsText,
    double caloriesPerServing,
    double proteinG,
    double carbsG,
    double fatG,
    double fiberG,
    double sodiumMg,
    double ironMg,
    double calciumMg,
    String status,
    int likeCount,
    int commentCount,
    int bookmarkCount,
    boolean isLikedByMe,
    boolean isBookmarkedByMe,
    List<CommunityIngredientDto> ingredients,
    Instant createdAt,
    String formattedDate) {

  public record CommunityIngredientDto(
      Long id,
      String foodId,
      String ingredientName,
      double amountG,
      String unit,
      boolean isVerifiedFood) {}
}
