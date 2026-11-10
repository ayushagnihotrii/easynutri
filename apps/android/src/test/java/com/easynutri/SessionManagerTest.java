package com.easynutri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.easynutri.core.network.SessionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(sdk = 34)
public class SessionManagerTest {

  private SessionManager sessionManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    sessionManager = new SessionManager(context);
    sessionManager.clearSession();
  }

  @Test
  public void testInitialStateIsLoggedOut() {
    assertFalse(sessionManager.isLoggedIn());
  }

  @Test
  public void testLoginSessionSavesUser() {
    sessionManager.saveLoginSession("testuser", "Test User", "ROLE_USER", "MOCK_SESSION_COOKIE");
    assertTrue(sessionManager.isLoggedIn());
    assertEquals("testuser", sessionManager.getUsername());
    assertEquals("Test User", sessionManager.getFullName());

    sessionManager.clearSession();
    assertFalse(sessionManager.isLoggedIn());
  }

  @Test
  public void testSensitiveModeToggle() {
    assertFalse(sessionManager.isSensitiveMode());
    sessionManager.setSensitiveMode(true);
    assertTrue(sessionManager.isSensitiveMode());
  }

  @Test
  public void testLanguagePreference() {
    assertEquals("en", sessionManager.getPreferredLanguage());
    sessionManager.setPreferredLanguage("hi");
    assertEquals("hi", sessionManager.getPreferredLanguage());
  }
}
