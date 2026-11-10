package com.easynutri.core.network;

import android.content.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

  private static ApiClient instance;
  private final ApiService apiService;
  private final SessionManager sessionManager;
  private final Map<String, List<Cookie>> cookieStore = new HashMap<>();

  private ApiClient(Context context) {
    this.sessionManager = new SessionManager(context.getApplicationContext());

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

    Interceptor dynamicUrlInterceptor =
        chain -> {
          Request originalRequest = chain.request();
          String currentBase = sessionManager.getBaseUrl();
          if (currentBase != null && !currentBase.trim().isEmpty()) {
            HttpUrl newBase = HttpUrl.parse(currentBase);
            if (newBase != null) {
              HttpUrl currentUrl = originalRequest.url();
              HttpUrl updatedUrl =
                  currentUrl
                      .newBuilder()
                      .scheme(newBase.scheme())
                      .host(newBase.host())
                      .port(newBase.port())
                      .build();
              Request newRequest = originalRequest.newBuilder().url(updatedUrl).build();
              return chain.proceed(newRequest);
            }
          }
          return chain.proceed(originalRequest);
        };

    CookieJar cookieJar =
        new CookieJar() {
          @Override
          public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
            for (Cookie c : cookies) {
              if ("JSESSIONID".equalsIgnoreCase(c.name())) {
                sessionManager.setSessionCookie(c.value());
              }
            }
          }

          @Override
          public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            if (cookies == null) {
              cookies = new ArrayList<>();
              String saved = sessionManager.getSessionCookie();
              if (saved != null && !saved.isEmpty()) {
                cookies.add(
                    new Cookie.Builder()
                        .domain(url.host())
                        .name("JSESSIONID")
                        .value(saved)
                        .path("/")
                        .build());
              }
            }
            return cookies;
          }
        };

    OkHttpClient okHttpClient =
        new OkHttpClient.Builder()
            .addInterceptor(dynamicUrlInterceptor)
            .cookieJar(cookieJar)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(sessionManager.getBaseUrl())
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    this.apiService = retrofit.create(ApiService.class);
  }

  public static synchronized ApiClient getInstance(Context context) {
    if (instance == null) {
      instance = new ApiClient(context);
    }
    return instance;
  }

  public ApiService getApiService() {
    return apiService;
  }

  public SessionManager getSessionManager() {
    return sessionManager;
  }
}
