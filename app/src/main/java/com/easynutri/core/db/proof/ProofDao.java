package com.easynutri.core.db.proof;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ProofDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  long insert(ProofEntity entity);

  @Query("SELECT * FROM proof_records ORDER BY id DESC")
  LiveData<List<ProofEntity>> getAllRecordsLive();

  @Query("SELECT * FROM proof_records ORDER BY id DESC")
  List<ProofEntity> getAllRecordsSync();

  @Query("SELECT COUNT(*) FROM proof_records")
  int getCount();
}
