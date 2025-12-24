package com.nexerp.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemRawRow {
  private Long itemId;
  private Long companyId;
  private String code;
  private String name;
  private Long quantity;
  private Long safetyStock;
}
