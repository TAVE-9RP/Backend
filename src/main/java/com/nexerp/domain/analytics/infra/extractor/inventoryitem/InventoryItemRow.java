package com.nexerp.domain.analytics.infra.extractor.inventoryitem;

import java.time.LocalDate;

public record InventoryItemRow(
  long inventoryItemId,
  long itemId,
  long inventoryId,
  long processedQuantity
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(inventoryItemId),
      String.valueOf(itemId),
      String.valueOf(inventoryId),
      String.valueOf(processedQuantity)
    };
  }
}
