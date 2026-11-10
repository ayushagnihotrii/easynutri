package com.easynutri.dto;

import java.util.List;

public record BatchSyncRequest(List<SyncActionDto> operations) {
  public BatchSyncRequest {
    operations = operations != null ? List.copyOf(operations) : List.of();
  }
}
