package com.nexerp.domain.inventory.model.response;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventorySummaryResponse {

  private Long inventoryId;
  private String projectNumber;
  private String inventoryTitle;
  private String itemSummary; // 애플망고 외 2개
  private String assigneeSummary; // 홍길동 외 3명
  private LocalDateTime requestedAt;
  private InventoryStatus inventoryStatus;

  public static InventorySummaryResponse from(
    Inventory inventory,
    String itemSummary,
    String assigneeSummary
  ) {
    return InventorySummaryResponse.builder()
      .inventoryId(inventory.getId())
      .projectNumber(inventory.getProject().getNumber())
      .inventoryTitle(inventory.getTitle())
      .itemSummary(itemSummary)
      .assigneeSummary(assigneeSummary)
      .requestedAt(inventory.getRequestedAt())
      .inventoryStatus(inventory.getStatus())
      .build();
  }
}
