package com.easynutri.core.db.sync;

import androidx.room.*;
import java.util.List;

@Dao
public interface SyncQueueDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(PendingSyncActionEntity action);

  @Query("SELECT * FROM pending_sync_actions WHERE status = 'PENDING' ORDER BY created_at ASC")
  List<PendingSyncActionEntity> getPendingActions();

  @Query(
      "UPDATE pending_sync_actions SET status = :status, retry_count = retry_count + 1 WHERE id = :id")
  void updateStatus(long id, String status);

  @Query("DELETE FROM pending_sync_actions WHERE id = :id")
  void deleteById(long id);

  @Query("DELETE FROM pending_sync_actions WHERE status = 'SYNCED'")
  void purgeSynced();

  @Query("SELECT COUNT(*) FROM pending_sync_actions WHERE status = 'PENDING'")
  int getPendingCount();
}
