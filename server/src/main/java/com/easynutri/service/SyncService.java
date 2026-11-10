package com.easynutri.service;

import com.easynutri.dto.*;
import com.easynutri.entity.SyncOperationEntity;
import com.easynutri.entity.UserEntity;
import com.easynutri.repository.GroceryItemRepository;
import com.easynutri.repository.PantryItemRepository;
import com.easynutri.repository.SyncOperationRepository;
import com.easynutri.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SyncService {

  private final SyncOperationRepository syncOperationRepository;
  private final UserRepository userRepository;
  private final ActiveProfileContextService activeProfileContextService;
  private final DiaryService diaryService;
  private final HydrationService hydrationService;
  private final ActivityTrackingService activityService;
  private final GroceryService groceryService;
  private final PantryService pantryService;
  private final PantryItemRepository pantryItemRepository;
  private final GroceryItemRepository groceryItemRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public SyncService(
      SyncOperationRepository syncOperationRepository,
      UserRepository userRepository,
      ActiveProfileContextService activeProfileContextService,
      DiaryService diaryService,
      HydrationService hydrationService,
      ActivityTrackingService activityService,
      GroceryService groceryService,
      PantryService pantryService,
      PantryItemRepository pantryItemRepository,
      GroceryItemRepository groceryItemRepository) {
    this.syncOperationRepository = syncOperationRepository;
    this.userRepository = userRepository;
    this.activeProfileContextService = activeProfileContextService;
    this.diaryService = diaryService;
    this.hydrationService = hydrationService;
    this.activityService = activityService;
    this.groceryService = groceryService;
    this.pantryService = pantryService;
    this.pantryItemRepository = pantryItemRepository;
    this.groceryItemRepository = groceryItemRepository;
  }

  public BatchSyncResponse processBatchSync(Long authenticatedUserId, BatchSyncRequest request) {
    UserEntity authUser =
        userRepository
            .findById(authenticatedUserId)
            .orElseThrow(
                () -> new IllegalArgumentException("User not found: " + authenticatedUserId));

    List<SyncResultItemDto> results = new ArrayList<>();
    int processedCount = 0;
    int conflictCount = 0;

    for (SyncActionDto op : request.operations()) {
      // 1. Idempotency Check
      if (syncOperationRepository.existsByIdempotencyKey(op.idempotencyKey())) {
        results.add(
            new SyncResultItemDto(
                op.operationId(),
                op.idempotencyKey(),
                "DUPLICATE_IGNORED",
                "Operation previously processed (idempotency preserved).",
                op.clientVersion()));
        continue;
      }

      // 2. Authorization Check (Profile Isolation)
      Long targetProfileId =
          op.targetProfileId() != null ? op.targetProfileId() : authenticatedUserId;
      if (!activeProfileContextService.canAccessProfile(authenticatedUserId, targetProfileId)) {
        recordSyncOperation(
            op, authUser, "REJECTED", "Unauthorized profile access for #" + targetProfileId);
        results.add(
            new SyncResultItemDto(
                op.operationId(),
                op.idempotencyKey(),
                "REJECTED",
                "Unauthorized access to profile #" + targetProfileId,
                op.clientVersion()));
        continue;
      }

      // 3. Process Action with Conflict Detection & Delete Propagation
      try {
        JsonNode node = objectMapper.readTree(op.payloadJson() != null ? op.payloadJson() : "{}");
        String actionType = op.actionType() != null ? op.actionType().toUpperCase() : "UNKNOWN";

        switch (actionType) {
          case "LOG_DIARY", "LOG_DIARY_ITEM" -> {
            String foodId = node.has("foodId") ? node.get("foodId").asText() : null;
            String recipeId = node.has("recipeId") ? node.get("recipeId").asText() : null;
            String customName = node.has("customName") ? node.get("customName").asText() : null;
            double amountG = node.has("amountG") ? node.get("amountG").asDouble() : 100.0;
            double servings = node.has("servings") ? node.get("servings").asDouble() : 1.0;
            String mealSlot = node.has("mealSlot") ? node.get("mealSlot").asText() : "LUNCH";
            LocalDate date =
                node.has("date") ? LocalDate.parse(node.get("date").asText()) : LocalDate.now();

            diaryService.logItem(
                targetProfileId,
                new LogFoodItemRequest(
                    date, mealSlot, foodId, recipeId, customName, amountG, servings));
          }
          case "DELETE_DIARY", "DELETE_DIARY_ITEM" -> {
            if (node.has("itemId")) {
              Long itemId = node.get("itemId").asLong();
              diaryService.deleteItem(targetProfileId, itemId);
            }
          }
          case "LOG_WATER" -> {
            int amountMl = node.has("amountMl") ? node.get("amountMl").asInt() : 250;
            String notes = node.has("notes") ? node.get("notes").asText() : "Synced from offline";
            LocalDate date =
                node.has("date") ? LocalDate.parse(node.get("date").asText()) : LocalDate.now();

            hydrationService.logWater(targetProfileId, new LogWaterRequest(date, amountMl, notes));
          }
          case "LOG_STEPS" -> {
            int steps = node.has("stepCount") ? node.get("stepCount").asInt() : 5000;
            LocalDate date =
                node.has("date") ? LocalDate.parse(node.get("date").asText()) : LocalDate.now();

            activityService.logSteps(
                targetProfileId, new LogStepsRequest(date, steps, 10000, "OFFLINE_SYNC"));
          }
          case "LOG_WORKOUT" -> {
            String activityType =
                node.has("activityType") ? node.get("activityType").asText() : "WALKING";
            int durationMinutes =
                node.has("durationMinutes") ? node.get("durationMinutes").asInt() : 30;
            String intensity = node.has("intensity") ? node.get("intensity").asText() : "MODERATE";
            LocalDate date =
                node.has("date") ? LocalDate.parse(node.get("date").asText()) : LocalDate.now();

            activityService.logWorkout(
                targetProfileId,
                new LogWorkoutRequest(
                    date,
                    activityType,
                    intensity,
                    durationMinutes,
                    null,
                    null,
                    "Synced from offline"));
          }
          case "UPDATE_GROCERY" -> {
            String foodId = node.has("foodId") ? node.get("foodId").asText() : null;
            String name = node.has("itemName") ? node.get("itemName").asText() : "Pantry Item";
            double qty = node.has("quantity") ? node.get("quantity").asDouble() : 1.0;
            String unit = node.has("unit") ? node.get("unit").asText() : "serving";
            String cat = node.has("category") ? node.get("category").asText() : "GENERAL";

            groceryService.addItem(
                targetProfileId, new AddGroceryItemRequest(name, qty, unit, cat, foodId));
          }
          case "TOGGLE_GROCERY" -> {
            if (node.has("itemId")) {
              Long itemId = node.get("itemId").asLong();
              var existing = groceryItemRepository.findByIdAndUserId(itemId, targetProfileId);
              if (existing.isEmpty()) {
                conflictCount++;
                recordSyncOperation(
                    op,
                    authUser,
                    "CONFLICT",
                    "Grocery item #" + itemId + " was removed on server.");
                results.add(
                    new SyncResultItemDto(
                        op.operationId(),
                        op.idempotencyKey(),
                        "CONFLICT_DETECTED",
                        "Grocery item #"
                            + itemId
                            + " was removed on server. Local checklist updated.",
                        op.clientVersion()));
                continue;
              }
              groceryService.toggleChecked(targetProfileId, itemId);
            }
          }
          case "DELETE_GROCERY", "DELETE_GROCERY_ITEM" -> {
            if (node.has("itemId")) {
              Long itemId = node.get("itemId").asLong();
              groceryService.deleteItem(targetProfileId, itemId);
            }
          }
          case "UPDATE_PANTRY" -> {
            String foodId = node.has("foodId") ? node.get("foodId").asText() : "food_spinach_palak";
            double qty = node.has("quantityG") ? node.get("quantityG").asDouble() : 250.0;
            LocalDate exp =
                node.has("expirationDate")
                    ? LocalDate.parse(node.get("expirationDate").asText())
                    : LocalDate.now().plusDays(3);

            pantryService.addPantryItem(
                targetProfileId,
                new AddPantryItemRequest(
                    foodId, qty, "g", "PANTRY", exp, LocalDate.now(), "Synced from offline"));
          }
          case "ADJUST_PANTRY" -> {
            if (node.has("itemId") && node.has("deltaG")) {
              Long itemId = node.get("itemId").asLong();
              double deltaG = node.get("deltaG").asDouble();
              var existingItem = pantryItemRepository.findByIdAndUserId(itemId, targetProfileId);
              if (existingItem.isEmpty()) {
                conflictCount++;
                recordSyncOperation(
                    op,
                    authUser,
                    "CONFLICT",
                    "Pantry item #" + itemId + " was already deleted or consumed on server.");
                results.add(
                    new SyncResultItemDto(
                        op.operationId(),
                        op.idempotencyKey(),
                        "CONFLICT_DETECTED",
                        "Pantry item #"
                            + itemId
                            + " was already consumed or deleted on server. Local state reconciled.",
                        op.clientVersion()));
                continue;
              }
              pantryService.adjustQuantity(targetProfileId, itemId, deltaG);
            }
          }
          case "DELETE_PANTRY", "DELETE_PANTRY_ITEM" -> {
            if (node.has("itemId")) {
              Long itemId = node.get("itemId").asLong();
              pantryService.deletePantryItem(targetProfileId, itemId);
            }
          }
          default -> throw new IllegalArgumentException("Unknown sync action: " + actionType);
        }

        recordSyncOperation(op, authUser, "PROCESSED", null);
        results.add(
            new SyncResultItemDto(
                op.operationId(),
                op.idempotencyKey(),
                "SUCCESS",
                "Action synchronized successfully.",
                op.clientVersion() + 1));
        processedCount++;

      } catch (Exception e) {
        recordSyncOperation(op, authUser, "REJECTED", e.getMessage());
        results.add(
            new SyncResultItemDto(
                op.operationId(),
                op.idempotencyKey(),
                "REJECTED",
                "Failed to process: " + e.getMessage(),
                op.clientVersion()));
      }
    }

    boolean allSuccess =
        conflictCount == 0
            && results.stream()
                .allMatch(
                    r -> "SUCCESS".equals(r.status()) || "DUPLICATE_IGNORED".equals(r.status()));
    return new BatchSyncResponse(allSuccess, processedCount, conflictCount, results);
  }

  private void recordSyncOperation(
      SyncActionDto op, UserEntity user, String status, String errorMessage) {
    SyncOperationEntity entity =
        new SyncOperationEntity(
            op.operationId() != null ? op.operationId() : UUID.randomUUID().toString(),
            op.idempotencyKey(),
            user,
            op.actionType(),
            op.payloadJson() != null ? op.payloadJson() : "{}",
            op.clientVersion(),
            status,
            errorMessage);
    syncOperationRepository.save(entity);
  }
}
