package com.nexerp.domain.logistics.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LogisticsItemsCreateRequest {

  @NotEmpty(message = "최소 하나 이상의 물품을 선택해야 합니다.")
  @Valid
  private List<CreateLogisticsItemDetail> items;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CreateLogisticsItemDetail {

    @NotNull(message = "물품(Inventory) ID는 필수입니다.")
    private Long itemId;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    @NotNull(message = "수량은 필수입니다.")
    private Long logisticsTargetedQuantity;
  }
}
