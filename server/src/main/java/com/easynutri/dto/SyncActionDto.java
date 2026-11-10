package com.easynutri.dto;

public record SyncActionDto(
    String operationId,
    String idempotencyKey,
    String
        actionType, // LOG_DIARY, LOG_WATER, LOG_WORKOUT, LOG_STEPS, UPDATE_GROCERY, UPDATE_PANTRY,
    // DELETE_DIARY
    Long targetProfileId,
    int clientVersion,
    String timestamp,
    String payloadJson) {}
