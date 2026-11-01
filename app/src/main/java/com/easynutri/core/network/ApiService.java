package com.easynutri.core.network;

import retrofit2.Call;
import retrofit2.http.GET;

/** Proof-of-life Retrofit API service interface for server communication. */
public interface ApiService {

  @GET("api/health")
  Call<ServerHealthResponse> checkHealth();

  record ServerHealthResponse(String status, String timestamp, String version) {}
}
