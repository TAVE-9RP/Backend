package com.nexerp.domain.item.model.response;

import com.nexerp.domain.item.model.entity.Item;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemDetailResponse {

  private Long itemId;
  private final String code;
  private final String name;
  private final Long quantity;
  private Long price;
  private String location;
  private final LocalDateTime createdAt;
  private Long targetStock;
  private Long safetyStock;

  public static ItemDetailResponse from(Item item) {
    return ItemDetailResponse.builder()
      .itemId(item.getId())
      .code(item.getCode())
      .name(item.getName())
      .quantity(item.getQuantity())
      .price(item.getPrice())
      .location(item.getLocation())
      .createdAt(item.getCreatedAt())
      .targetStock(item.getTargetStock())
      .safetyStock(item.getSafetyStock())
      .build();
  }
}
