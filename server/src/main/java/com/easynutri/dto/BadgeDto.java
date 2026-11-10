package com.easynutri.dto;

import java.time.Instant;

public record BadgeDto(
    String id,
    String name,
    String description,
    String icon,
    String category,
    boolean isUnlocked,
    Instant awardedAt,
    String formattedAwardDate) {}
