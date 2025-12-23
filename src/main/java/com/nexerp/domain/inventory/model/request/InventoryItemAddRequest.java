package com.nexerp.domain.inventory.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryItemAddRequest {

  @NotEmpty(message = "추가할 품목 리스트는 필수입니다.")
  private List<Long> itemIds;
}
