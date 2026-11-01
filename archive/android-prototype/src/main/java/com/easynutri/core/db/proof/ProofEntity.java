package com.easynutri.core.db.proof;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Proof-of-life Room entity verifying SQLite schema compilation and migration support in Phase 0.
 */
@Entity(tableName = "proof_records")
public final class ProofEntity {

  @PrimaryKey(autoGenerate = true)
  private final long id;

  @ColumnInfo(name = "title")
  private final String title;

  @ColumnInfo(name = "created_at")
  private final long createdAt;

  public ProofEntity(long id, String title, long createdAt) {
    this.id = id;
    this.title = title;
    this.createdAt = createdAt;
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public long getCreatedAt() {
    return createdAt;
  }
}
