package com.nexerp.domain.inventory.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InventoryItemAddResponse {
  private List<Long> inventoryItemIds;

  public static InventoryItemAddResponse from (List<Long> ids) {
    return InventoryItemAddResponse.builder()
      .inventoryItemIds(ids)
      .build();
  }
}
