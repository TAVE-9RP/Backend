package com.nexerp.domain.inventory.model.response;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.inventoryitem.model.enums.InventoryProcessingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryItemResponse {

  private final Long inventoryItemId;
  private final Long itemId;
  private final String itemCode;
  private final String itemName;
  private final Long itemPrice;
  private final Long targetQuantity;
  private final Long processedQuantity;
  private InventoryProcessingStatus inventoryProcessingStatus;

  public static InventoryItemResponse from(InventoryItem item) {
    return InventoryItemResponse.builder()
      .inventoryItemId(item.getId())
      .itemId(item.getItem().getId())
      .itemCode(item.getItem().getCode())
      .itemName(item.getItem().getName())
      .itemPrice(item.getItem().getPrice())
      .targetQuantity(item.getQuantity())
      .processedQuantity(item.getProcessed_quantity())
      .inventoryProcessingStatus(item.getStatus())
      .build();
  }
}
