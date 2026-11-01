package com.easynutri.core.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.easynutri.core.db.proof.ProofDao;
import com.easynutri.core.db.proof.ProofEntity;

/**
 * Root Room database for the EasyNutri Android application. Explicit versioned migrations are
 * strictly enforced. Never use fallbackToDestructiveMigration().
 */
@Database(
    entities = {ProofEntity.class},
    version = 1,
    exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
  public abstract ProofDao proofDao();
}
