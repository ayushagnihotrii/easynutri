package com.easynutri.core.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.easynutri.core.db.proof.ProofDao;
import com.easynutri.core.db.proof.ProofEntity;
import com.easynutri.core.db.sync.PendingSyncActionEntity;
import com.easynutri.core.db.sync.SyncQueueDao;

@Database(
    entities = {ProofEntity.class, PendingSyncActionEntity.class},
    version = 2,
    exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
  public abstract ProofDao proofDao();

  public abstract SyncQueueDao syncQueueDao();
}
