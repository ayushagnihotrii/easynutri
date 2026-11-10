package com.easynutri.core.network;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

  private static final String PREF_NAME = "easynutri_session";
  private static final String KEY_IS_LOGGED_IN = "is_logged_in";
  private static final String KEY_USERNAME = "username";
  private static final String KEY_FULL_NAME = "full_name";
  private static final String KEY_ROLE = "role";
  private static final String KEY_SESSION_COOKIE = "session_cookie";
  private static final String KEY_BASE_URL = "base_url";
  private static final String KEY_SENSITIVE_MODE = "sensitive_mode";
  private static final String KEY_LANGUAGE = "language";

  // Default emulator IP pointing to host machine Spring Boot server
  public static final String DEFAULT_BASE_URL = "http://10.0.2.2:8080/";

  private final SharedPreferences prefs;

  public SessionManager(Context context) {
    this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  public void saveLoginSession(String username, String fullName, String role, String cookie) {
    prefs
        .edit()
        .putBoolean(KEY_IS_LOGGED_IN, true)
        .putString(KEY_USERNAME, username)
        .putString(KEY_FULL_NAME, fullName)
        .putString(KEY_ROLE, role)
        .putString(KEY_SESSION_COOKIE, cookie)
        .apply();
  }

  public boolean isLoggedIn() {
    return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
  }

  public String getUsername() {
    return prefs.getString(KEY_USERNAME, "demo");
  }

  public String getFullName() {
    return prefs.getString(KEY_FULL_NAME, "EasyNutri User");
  }

  public String getRole() {
    return prefs.getString(KEY_ROLE, "ROLE_USER");
  }

  public String getSessionCookie() {
    return prefs.getString(KEY_SESSION_COOKIE, "");
  }

  public void setSessionCookie(String cookie) {
    prefs.edit().putString(KEY_SESSION_COOKIE, cookie).apply();
  }

  public String getBaseUrl() {
    return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL);
  }

  public void setBaseUrl(String url) {
    if (url != null && !url.endsWith("/")) {
      url += "/";
    }
    prefs.edit().putString(KEY_BASE_URL, url).apply();
  }

  public boolean isSensitiveMode() {
    return prefs.getBoolean(KEY_SENSITIVE_MODE, false);
  }

  public void setSensitiveMode(boolean enabled) {
    prefs.edit().putBoolean(KEY_SENSITIVE_MODE, enabled).apply();
  }

  public String getLanguage() {
    return prefs.getString(KEY_LANGUAGE, "en");
  }

  public String getPreferredLanguage() {
    return getLanguage();
  }

  public void setLanguage(String lang) {
    prefs.edit().putString(KEY_LANGUAGE, lang).apply();
  }

  public void setPreferredLanguage(String lang) {
    setLanguage(lang);
  }

  public void clearSession() {
    prefs
        .edit()
        .remove(KEY_IS_LOGGED_IN)
        .remove(KEY_USERNAME)
        .remove(KEY_FULL_NAME)
        .remove(KEY_ROLE)
        .remove(KEY_SESSION_COOKIE)
        .apply();
  }
}
