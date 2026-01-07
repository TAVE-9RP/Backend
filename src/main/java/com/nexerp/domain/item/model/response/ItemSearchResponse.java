package com.nexerp.domain.item.model.response;

import com.nexerp.domain.item.model.entity.Item;
import java.time.LocalDate;
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
  private final LocalDate receivedAt;
  private final LocalDate createdAt;

  public static ItemSearchResponse from(Item i) {
    return ItemSearchResponse.builder()
      .itemId(i.getId())
      .code(i.getCode())
      .name(i.getName())
      .quantity(i.getQuantity())
      .location(i.getLocation())
      .price(i.getPrice())
      .receivedAt(
        i.getReceivedAt() != null
          ? i.getReceivedAt().toLocalDate()
          : null
      )
      .createdAt(i.getCreatedAt().toLocalDate())
      .build();
  }
}
