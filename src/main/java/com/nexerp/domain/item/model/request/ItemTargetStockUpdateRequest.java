package com.nexerp.domain.item.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

@Getter
public class ItemTargetStockUpdateRequest {

  @NotNull(message = "목표 수량은 필수입니다.")
  @PositiveOrZero(message = "수량은 0 이상이어야 합니다.")
  private Long targetStock;
}
