package com.nexerp.domain.inventory.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class InventoryProcessRequest {

  @NotEmpty(message = "입고 처리 정보(process)는 최소 1개 이상이어야 합니다.")
  private List<@Valid ProcessUnit> process;

  @Getter
  public static class ProcessUnit {

    @NotNull(message = "inventoryItemId는 필수입니다.")
    private Long inventoryItemId;

    @NotNull(message = "receiveQuantity는 필수입니다.")
    @Min(value = 1, message = "입고 처리 수량은 최소 1 이상이어야 합니다.")
    private Long receiveQuantity;
  }
}
