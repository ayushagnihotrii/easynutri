package com.easynutri.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DomainArchitectureTest {

  @Test
  @DisplayName("Verify domain module source files have zero Android or Spring framework imports")
  void verifyDomainHasNoAndroidOrSpringImports() throws IOException {
    Path srcDir = Path.of("src", "main", "java");
    assertTrue(Files.exists(srcDir), "Domain source directory must exist");

    try (Stream<Path> paths = Files.walk(srcDir)) {
      List<Path> javaFiles = paths.filter(p -> p.toString().endsWith(".java")).toList();
      assertFalse(javaFiles.isEmpty(), "Domain module must contain Java files");

      for (Path javaFile : javaFiles) {
        List<String> lines = Files.readAllLines(javaFile);
        for (String line : lines) {
          String trimmed = line.trim();
          if (trimmed.startsWith("import ")) {
            assertFalse(
                trimmed.startsWith("import android."),
                "Illegal Android import found in " + javaFile + ": " + trimmed);
            assertFalse(
                trimmed.startsWith("import androidx."),
                "Illegal AndroidX import found in " + javaFile + ": " + trimmed);
            assertFalse(
                trimmed.startsWith("import org.springframework."),
                "Illegal Spring import found in " + javaFile + ": " + trimmed);
          }
        }
      }
    }
  }
}
