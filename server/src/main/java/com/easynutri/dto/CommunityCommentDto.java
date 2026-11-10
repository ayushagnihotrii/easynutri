package com.easynutri.dto;

import java.time.Instant;

public record CommunityCommentDto(
    Long id,
    Long recipeId,
    Long authorId,
    String authorUsername,
    String authorAvatar,
    boolean isAuthorPro,
    String authorProCategory,
    String content,
    String status,
    boolean isOwnComment,
    Instant createdAt,
    String formattedDate) {}
