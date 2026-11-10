package com.easynutri.dto;

import java.util.List;

public record BatchSyncResponse(
    boolean allSuccessful,
    int processedCount,
    int conflictCount,
    List<SyncResultItemDto> results) {}
