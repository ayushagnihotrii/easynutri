package com.easynutri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.easynutri.core.db.AppDatabase;
import com.easynutri.core.db.proof.ProofDao;
import com.easynutri.core.db.proof.ProofEntity;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class ProofDaoTest {

  private AppDatabase database;
  private ProofDao proofDao;

  @Before
  public void createDb() {
    Context context = ApplicationProvider.getApplicationContext();
    database =
        Room.inMemoryDatabaseBuilder(context, AppDatabase.class).allowMainThreadQueries().build();
    proofDao = database.proofDao();
  }

  @After
  public void closeDb() throws IOException {
    database.close();
  }

  @Test
  public void testInsertAndRetrieveRecord() {
    long now = System.currentTimeMillis();
    ProofEntity record = new ProofEntity(0, "Phase 0 Verification Entity", now);
    long insertedId = proofDao.insert(record);

    assertEquals(1, proofDao.getCount());
    List<ProofEntity> records = proofDao.getAllRecordsSync();
    assertNotNull(records);
    assertEquals(1, records.size());
    assertEquals("Phase 0 Verification Entity", records.get(0).getTitle());
  }
}
