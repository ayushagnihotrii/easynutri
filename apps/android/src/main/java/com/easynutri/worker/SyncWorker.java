package com.easynutri.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.easynutri.core.db.AppDatabase;
import com.easynutri.core.db.sync.PendingSyncActionEntity;
import com.easynutri.core.di.DatabaseModule;
import com.easynutri.core.network.ApiClient;
import com.easynutri.core.network.ApiService;
import com.easynutri.data.model.Models.*;
import com.google.gson.Gson;
import java.util.List;

public class SyncWorker extends Worker {

  private final AppDatabase database;
  private final ApiService apiService;
  private final Gson gson = new Gson();

  public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
    super(context, workerParams);
    this.database = DatabaseModule.provideAppDatabase(context);
    this.apiService = ApiClient.getInstance(context).getApiService();
  }

  @NonNull
  @Override
  public Result doWork() {
    List<PendingSyncActionEntity> pending = database.syncQueueDao().getPendingActions();
    if (pending.isEmpty()) {
      return Result.success();
    }

    for (PendingSyncActionEntity action : pending) {
      try {
        boolean success = false;
        if ("LOG_DIARY".equals(action.actionType)) {
          LogFoodItemRequest req = gson.fromJson(action.payloadJson, LogFoodItemRequest.class);
          var res = apiService.logDiaryItem(req).execute();
          success = res.isSuccessful();
        } else if ("LOG_WATER".equals(action.actionType)) {
          LogWaterRequest req = gson.fromJson(action.payloadJson, LogWaterRequest.class);
          var res = apiService.logWater(req).execute();
          success = res.isSuccessful();
        } else if ("LOG_STEPS".equals(action.actionType)) {
          LogStepsRequest req = gson.fromJson(action.payloadJson, LogStepsRequest.class);
          var res = apiService.logSteps(req).execute();
          success = res.isSuccessful();
        } else if ("LOG_WORKOUT".equals(action.actionType)) {
          LogWorkoutRequest req = gson.fromJson(action.payloadJson, LogWorkoutRequest.class);
          var res = apiService.logWorkout(req).execute();
          success = res.isSuccessful();
        } else if ("ADJUST_PANTRY".equals(action.actionType)) {
          AddPantryItemRequest req = gson.fromJson(action.payloadJson, AddPantryItemRequest.class);
          var res = apiService.addPantryItem(req).execute();
          success = res.isSuccessful();
        }

        if (success) {
          database.syncQueueDao().updateStatus(action.id, "SYNCED");
        } else {
          database.syncQueueDao().updateStatus(action.id, "FAILED");
        }
      } catch (Exception e) {
        database.syncQueueDao().updateStatus(action.id, "FAILED");
      }
    }

    database.syncQueueDao().purgeSynced();
    return Result.success();
  }
}
