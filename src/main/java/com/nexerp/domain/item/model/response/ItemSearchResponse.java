package com.nexerp.domain.item.model.response;

import com.nexerp.domain.item.model.entity.Item;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemSearchResponse {

  private final Long itemId;
  private final String code;
  private final String name;
  private final Long quantity;
  private final String location;
  private final Long price;

  public static ItemSearchResponse from (Item i) {
    return ItemSearchResponse.builder()
      .itemId(i.getId())
      .code(i.getCode())
      .name(i.getName())
      .quantity(i.getQuantity())
      .location(i.getLocation())
      .price(i.getPrice())
      .build();
  }
}
