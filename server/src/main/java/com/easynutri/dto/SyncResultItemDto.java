package com.easynutri.dto;

public record SyncResultItemDto(
    String operationId,
    String idempotencyKey,
    String status, // SUCCESS, DUPLICATE_IGNORED, CONFLICT, REJECTED
    String message,
    int serverVersion) {}
