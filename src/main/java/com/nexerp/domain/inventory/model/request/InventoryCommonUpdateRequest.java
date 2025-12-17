package com.nexerp.domain.inventory.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InventoryCommonUpdateRequest {

  @NotBlank(message = "입고 업무명을 입력하세요.")
  private String title;

  @NotBlank(message = "업무 설명을 입력하세요.")
  private String description;

  // 요청일은 사용자가 아닌 백엔드 통해서 처리
}
