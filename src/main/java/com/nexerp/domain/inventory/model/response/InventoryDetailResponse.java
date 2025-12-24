package com.nexerp.domain.inventory.model.response;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InventoryDetailResponse {

  private String projectNumber;
  private List<String> inventoryAssignees;
  private String inventoryTitle;
  private String inventoryDescription;
  private LocalDateTime inventoryRequestedAt;
  private InventoryStatus inventoryStatus;

  public static InventoryDetailResponse from(
    Inventory inventory,
    List<String> assignees
  ) {
    return InventoryDetailResponse.builder()
      .projectNumber(inventory.getProject().getNumber())
      .inventoryAssignees(assignees)
      .inventoryTitle(inventory.getTitle())
      .inventoryDescription(inventory.getDescription())
      .inventoryRequestedAt(inventory.getRequestedAt())
      .inventoryStatus(inventory.getStatus())
      .build();
  }
}
