package com.easynutri.dto;

import java.time.Instant;

public record UserNotificationDto(
    Long id, String title, String message, String type, boolean read, Instant createdAt) {}
