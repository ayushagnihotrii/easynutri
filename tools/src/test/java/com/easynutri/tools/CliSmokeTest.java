package com.easynutri.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CliSmokeTest {

  @Test
  @DisplayName("Verify CLI helper version")
  void testCliVersion() {
    assertEquals("1.0.0", DataImporterCli.getVersion());
  }
}
