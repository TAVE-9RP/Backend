package com.nexerp.domain.analytics.infra.extractor.item;

import java.time.LocalDate;

public record ItemRow(
  long itemId,
  Long itemQuantity,
  Long safetyStock
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(itemId),
      itemQuantity != null ? itemQuantity.toString() : "",
      safetyStock != null ? safetyStock.toString() : ""
    };
  }
}
