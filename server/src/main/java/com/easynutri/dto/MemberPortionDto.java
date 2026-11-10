package com.easynutri.dto;

public record MemberPortionDto(
    Long userId, String displayName, double servings, double calories, double proteinG) {}
