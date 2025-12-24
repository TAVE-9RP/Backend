package com.nexerp.domain.inventory.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryTargetQuantityUpdateRequest {

  @NotEmpty
  private List<QuantityUpdateUnit> updates;

  @Getter
  public static class QuantityUpdateUnit {

    @NotNull(message = "목표 수량을 설정할 품목의 id는 필수입니다.")
    private Long inventoryItemId;

    @NotNull
    @Min(value = 1, message = "목표 수량을 최소 1개 이상 입력해주세요.")
    private Long targetQuantity;
  }
}
