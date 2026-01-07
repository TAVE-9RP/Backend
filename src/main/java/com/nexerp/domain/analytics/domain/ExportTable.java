package com.nexerp.domain.analytics.domain;

public enum ExportTable {

  PROJECT("project"),
  INVENTORY("inventory"),
  INVENTORY_ITEM("inventory_item"),
  LOGISTICS("logistics"),
  LOGISTICS_ITEM("logistics_item"),
  ITEM("item");

  private final String filePrefix;

  ExportTable(String filePrefix) {
    this.filePrefix = filePrefix;
  }

  public String filePrefix() {
    return filePrefix;
  }
}
