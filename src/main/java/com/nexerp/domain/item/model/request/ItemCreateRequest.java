package com.nexerp.domain.item.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ItemCreateRequest {

  @NotBlank(message = "재고 번호(itemCode)는 필수입니다.")
  private String code;

  @NotBlank(message = "품목명(name)은 필수입니다.")
  private String name;

  @NotBlank(message = "위치(location)는 필수입니다.")
  private String location;

  @NotNull(message = "물품 가격(price)은 필수입니다.")
  private Long price;
}
