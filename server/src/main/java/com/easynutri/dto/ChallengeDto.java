package com.easynutri.dto;

import java.time.Instant;

public record ChallengeDto(
    String id,
    String title,
    String description,
    String icon,
    String category,
    int targetCount,
    int durationDays,
    int progress,
    int progressPercent,
    boolean isJoined,
    boolean isCompleted,
    Instant completedAt,
    long participantCount) {}
