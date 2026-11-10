package com.easynutri.dto;

import java.util.List;

public record CreatorProfileDto(
    Long userId,
    String username,
    String fullName,
    String bio,
    String avatarUrl,
    boolean isVerifiedPro,
    String proCategory,
    long approvedRecipeCount,
    long followersCount,
    long followingCount,
    boolean isFollowedByMe,
    boolean isSelf,
    List<CommunityRecipeDto> recipes) {}
