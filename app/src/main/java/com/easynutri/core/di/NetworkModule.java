package com.easynutri.core.di;

import com.easynutri.core.network.ApiClient;
import com.easynutri.core.network.ApiService;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

  @Provides
  @Singleton
  public static OkHttpClient provideOkHttpClient() {
    return ApiClient.createOkHttpClient();
  }

  @Provides
  @Singleton
  public static Retrofit provideRetrofit(OkHttpClient okHttpClient) {
    return ApiClient.createRetrofit(okHttpClient);
  }

  @Provides
  @Singleton
  public static ApiService provideApiService(Retrofit retrofit) {
    return retrofit.create(ApiService.class);
  }
}
