package com.nexerp.domain.inventory.model.response;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.global.common.model.TaskStatus;
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
  private TaskStatus inventoryStatus;

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
