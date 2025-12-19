package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logisticsItem.model.enums.LogisticsProcessingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsItemResponse {

  Long itemId;

  //물품 코드
  String itemCode;

  //물품이름
  String itemName;

  //물품 출하량
  Long processedQuantity;

  //목표 출하 수량
  Long targetedQuantity;

  //물품 가격
  Long itemPrice;

  // 단위
  String unitOfMeasure;

  LogisticsProcessingStatus logisticsProcessingStatus;
}
