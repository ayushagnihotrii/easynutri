package com.easynutri.core.di;

import android.content.Context;
import androidx.room.Room;
import com.easynutri.core.db.AppDatabase;
import com.easynutri.core.db.proof.ProofDao;
import com.easynutri.core.db.sync.SyncQueueDao;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public final class DatabaseModule {

  @Provides
  @Singleton
  public static AppDatabase provideAppDatabase(@ApplicationContext Context context) {
    return Room.databaseBuilder(context, AppDatabase.class, "easynutri.db")
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  public static ProofDao provideProofDao(AppDatabase db) {
    return db.proofDao();
  }

  @Provides
  public static SyncQueueDao provideSyncQueueDao(AppDatabase db) {
    return db.syncQueueDao();
  }
}
