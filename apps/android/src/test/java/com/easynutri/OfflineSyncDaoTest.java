package com.easynutri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.easynutri.core.db.AppDatabase;
import com.easynutri.core.db.sync.PendingSyncActionEntity;
import com.easynutri.core.db.sync.SyncQueueDao;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class OfflineSyncDaoTest {

  private AppDatabase database;
  private SyncQueueDao syncQueueDao;

  @Before
  public void createDb() {
    Context context = ApplicationProvider.getApplicationContext();
    database =
        Room.inMemoryDatabaseBuilder(context, AppDatabase.class).allowMainThreadQueries().build();
    syncQueueDao = database.syncQueueDao();
  }

  @After
  public void closeDb() throws IOException {
    database.close();
  }

  @Test
  public void testInsertAndRetrievePendingSync() {
    PendingSyncActionEntity action =
        new PendingSyncActionEntity("sync-101", "LOG_WATER", "{\"amountMl\":250}");

    long generatedId = syncQueueDao.insert(action);

    assertEquals(1, syncQueueDao.getPendingCount());
    List<PendingSyncActionEntity> pending = syncQueueDao.getPendingActions();
    assertNotNull(pending);
    assertEquals(1, pending.size());
    assertEquals("sync-101", pending.get(0).clientUuid);
    assertEquals("LOG_WATER", pending.get(0).actionType);

    syncQueueDao.deleteById(generatedId);
    assertEquals(0, syncQueueDao.getPendingCount());
  }
}
