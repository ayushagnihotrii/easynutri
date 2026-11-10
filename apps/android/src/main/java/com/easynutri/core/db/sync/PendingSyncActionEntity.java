package com.easynutri.core.db.sync;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_sync_actions")
public class PendingSyncActionEntity {

  @PrimaryKey(autoGenerate = true)
  public long id;

  @ColumnInfo(name = "client_uuid")
  public String clientUuid;

  @ColumnInfo(name = "action_type")
  public String actionType; // LOG_DIARY, LOG_WATER, LOG_STEPS, ADJUST_PANTRY, TOGGLE_GROCERY

  @ColumnInfo(name = "payload_json")
  public String payloadJson;

  @ColumnInfo(name = "status")
  public String status; // PENDING, SYNCING, FAILED, SYNCED

  @ColumnInfo(name = "created_at")
  public long createdAt;

  @ColumnInfo(name = "retry_count")
  public int retryCount;

  public PendingSyncActionEntity(String clientUuid, String actionType, String payloadJson) {
    this.clientUuid = clientUuid;
    this.actionType = actionType;
    this.payloadJson = payloadJson;
    this.status = "PENDING";
    this.createdAt = System.currentTimeMillis();
    this.retryCount = 0;
  }
}
