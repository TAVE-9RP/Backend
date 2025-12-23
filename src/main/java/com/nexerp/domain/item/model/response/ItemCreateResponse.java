package com.nexerp.domain.item.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemCreateResponse {

  private Long itemId;

  public static ItemCreateResponse from(Long id) {
    return ItemCreateResponse.builder()
      .itemId(id)
      .build();
  }
}
