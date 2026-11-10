package com.easynutri.dto;

public record ModerationDecisionRequest(
    String targetType, Long targetId, String actionTaken, String moderatorNotes) {}
