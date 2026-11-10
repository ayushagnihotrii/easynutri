package com.easynutri.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Server-side audit ledger of offline-first synchronizations enforcing idempotency and preventing
 * duplicate replay.
 */
@Entity
@Table(name = "sync_operations")
public class SyncOperationEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "operation_id", nullable = false, length = 100)
  private String operationId;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 100)
  private String idempotencyKey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(name = "action_type", nullable = false, length = 50)
  private String
      actionType; // LOG_DIARY, LOG_WATER, LOG_WORKOUT, LOG_STEPS, UPDATE_GROCERY, UPDATE_PANTRY,

  // DELETE_DIARY

  @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
  private String payloadJson;

  @Column(name = "client_version", nullable = false)
  private int clientVersion = 1;

  @Column(nullable = false, length = 30)
  private String status = "PROCESSED"; // PROCESSED, DUPLICATE_IGNORED, CONFLICT_DETECTED, REJECTED

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt = Instant.now();

  public SyncOperationEntity() {}

  public SyncOperationEntity(
      String operationId,
      String idempotencyKey,
      UserEntity user,
      String actionType,
      String payloadJson,
      int clientVersion,
      String status,
      String errorMessage) {
    this.operationId = operationId;
    this.idempotencyKey = idempotencyKey;
    this.user = user;
    this.actionType = actionType;
    this.payloadJson = payloadJson;
    this.clientVersion = clientVersion;
    this.status = status;
    this.errorMessage = errorMessage;
    this.processedAt = Instant.now();
  }

  public Long getId() {
    return id;
  }

  public String getOperationId() {
    return operationId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public UserEntity getUser() {
    return user;
  }

  public String getActionType() {
    return actionType;
  }

  public String getPayloadJson() {
    return payloadJson;
  }

  public int getClientVersion() {
    return clientVersion;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public Instant getProcessedAt() {
    return processedAt;
  }
}
