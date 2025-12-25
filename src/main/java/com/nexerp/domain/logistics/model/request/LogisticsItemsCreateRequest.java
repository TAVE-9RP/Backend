package com.nexerp.domain.logistics.model.request;

import jakarta.validation.constraints.NotEmpty;
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
  private List<Long> itemIds;
}
