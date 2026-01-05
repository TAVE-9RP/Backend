package com.nexerp.domain.analytics.infra.extractor.logisticsitem;

import java.time.LocalDate;

public record LogisticsItemRow(
  long logisticsItemId,
  long itemId,
  long logisticsId,
  long processedQuantity
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(logisticsItemId),
      String.valueOf(itemId),
      String.valueOf(logisticsId),
      String.valueOf(processedQuantity)
    };
  }
}
