package com.easynutri.controller.api;

import com.easynutri.dto.BatchSyncRequest;
import com.easynutri.dto.BatchSyncResponse;
import com.easynutri.entity.SyncOperationEntity;
import com.easynutri.repository.SyncOperationRepository;
import com.easynutri.service.ActiveProfileContextService;
import com.easynutri.service.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sync")
@Tag(
    name = "Offline Synchronization",
    description =
        "Endpoints for offline-first PWA action synchronization, idempotency, and conflict detection")
public class SyncApiController {

  private final SyncService syncService;
  private final SyncOperationRepository syncOperationRepository;
  private final ActiveProfileContextService activeProfileContextService;

  public SyncApiController(
      SyncService syncService,
      SyncOperationRepository syncOperationRepository,
      ActiveProfileContextService activeProfileContextService) {
    this.syncService = syncService;
    this.syncOperationRepository = syncOperationRepository;
    this.activeProfileContextService = activeProfileContextService;
  }

  @PostMapping("/batch")
  @Operation(summary = "Process a batch of offline-queued operations with strict idempotency")
  public ResponseEntity<BatchSyncResponse> processBatchSync(
      @RequestBody BatchSyncRequest request, Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    return ResponseEntity.ok(syncService.processBatchSync(authUserId, request));
  }

  @GetMapping("/history")
  @Operation(summary = "Get audit history of processed synchronization operations for the user")
  public ResponseEntity<List<SyncOperationEntity>> getSyncHistory(Principal principal) {
    Long authUserId = activeProfileContextService.getAuthenticatedUserId(principal);
    return ResponseEntity.ok(
        syncOperationRepository.findByUserIdOrderByProcessedAtDesc(authUserId));
  }
}
