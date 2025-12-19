package com.nexerp.domain.item.model.response;

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
}
