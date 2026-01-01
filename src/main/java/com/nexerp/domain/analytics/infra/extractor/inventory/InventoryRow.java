package com.nexerp.domain.analytics.infra.extractor.inventory;

import java.time.LocalDate;

public record InventoryRow(
  long inventoryId,
  long projectId,
  LocalDate requestedAt,
  String status,
  LocalDate completedAt
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(inventoryId),
      String.valueOf(projectId),
      requestedAt != null ? requestedAt.toString() : "",
      status != null ? status : "",
      completedAt != null ? completedAt.toString() : ""
    };
  }
}
