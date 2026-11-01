package com.easynutri.tools;

/**
 * Command-line utility harness for importing and processing nutrition datasets (IFCT, USDA, Open
 * Food Facts). Will be extended in Phase 1 with specific importers.
 */
public final class DataImporterCli {

  private DataImporterCli() {}

  public static void main(String[] args) {
    System.out.println("EasyNutri Data Importer CLI v1.0.0");
    System.out.println("Status: Scaffolding initialized. Use specific subcommands in Phase 1.");
  }

  public static String getVersion() {
    return "1.0.0";
  }
}
