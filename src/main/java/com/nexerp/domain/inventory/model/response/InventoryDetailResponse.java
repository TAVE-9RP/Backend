package com.nexerp.domain.inventory.model.response;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventory.model.enums.InventoryStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InventoryDetailResponse {

  private Long inventoryId;
  private String projectNumber;
  private List<String> assignees;
  private String title;
  private LocalDateTime requestedAt;
  private String description;
  private InventoryStatus status;

  public static InventoryDetailResponse from(
    Inventory inventory,
    List<String> assignees
  ) {
    return InventoryDetailResponse.builder()
      .inventoryId(inventory.getId())
      .projectNumber(inventory.getProject().getNumber())
      .assignees(assignees)
      .title(inventory.getTitle())
      .requestedAt(inventory.getRequestedAt())
      .description(inventory.getDescription())
      .status(inventory.getStatus())
      .build();
  }
}
