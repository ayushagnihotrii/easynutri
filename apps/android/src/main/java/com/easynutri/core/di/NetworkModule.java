package com.easynutri.core.di;

import android.content.Context;
import com.easynutri.core.network.ApiClient;
import com.easynutri.core.network.ApiService;
import com.easynutri.core.network.SessionManager;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

  @Provides
  @Singleton
  public static ApiClient provideApiClient(@ApplicationContext Context context) {
    return ApiClient.getInstance(context);
  }

  @Provides
  @Singleton
  public static ApiService provideApiService(ApiClient apiClient) {
    return apiClient.getApiService();
  }

  @Provides
  @Singleton
  public static SessionManager provideSessionManager(ApiClient apiClient) {
    return apiClient.getSessionManager();
  }
}
