package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.logisticsItem.model.entity.LogisticsItem;
import com.nexerp.domain.logisticsItem.model.enums.LogisticsProcessingStatus;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsItemResponse {

  private final Long itemId;

  //물품 코드
  private final String itemCode;

  //물품이름
  private final String itemName;

  //물품 출하량
  private final Long processedQuantity;

  //목표 출하 수량
  private final Long targetedQuantity;

  //물품 가격
  private final Long itemPrice;

  // 물품 총 가격
  private final BigDecimal itemTotalPrice;

  // 단위
  private final String unitOfMeasure;

  private final LogisticsProcessingStatus logisticsProcessingStatus;

  public static LogisticsItemResponse from(LogisticsItem logisticsItem) {
    Item item = logisticsItem.getItem();

    return LogisticsItemResponse.builder()
      .itemId(item.getId())
      .itemCode(item.getCode())
      .itemName(item.getName())
      .processedQuantity(logisticsItem.getProcessedQuantity())
      .targetedQuantity(logisticsItem.getTargetedQuantity())
      .itemPrice(item.getPrice())
      .itemTotalPrice(logisticsItem.getTotalPrice())
      .unitOfMeasure(item.getUnitOfMeasure())
      .logisticsProcessingStatus(logisticsItem.getProcessingStatus())
      .build();
  }

  public static List<LogisticsItemResponse> fromList(List<LogisticsItem> logisticsItems) {
    return logisticsItems.stream()
      .map(LogisticsItemResponse::from)
      .toList();
  }
}
