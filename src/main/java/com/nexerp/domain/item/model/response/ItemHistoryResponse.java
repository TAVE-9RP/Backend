package com.nexerp.domain.item.model.response;

import com.nexerp.domain.item.model.entity.ItemHistory;
import com.nexerp.domain.item.model.enums.TaskType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemHistoryResponse {

  private final Long itemHistoryId;

  private final Long itemId;
  // INVENTORY / LOGISTICS
  private final TaskType taskType;
  private final Long memberId;
  private final String memberName;
  private final LocalDateTime processedAt;
  private final Long changeQuantity;       // 처리 수량

  public static ItemHistoryResponse from(ItemHistory history) {
    return ItemHistoryResponse.builder()
      .itemHistoryId(history.getId())
      .itemId(history.getItem().getId())
      .taskType(history.getTaskType())
      .memberId(history.getMember().getId())
      .memberName(history.getMember().getName())
      .processedAt(history.getProcessedAt())
      .changeQuantity(history.getChangeQuantity())
      .build();
  }

  public static List<ItemHistoryResponse> fromList(List<ItemHistory> histories) {
    return histories.stream()
      .map(ItemHistoryResponse::from)
      .collect(Collectors.toList());
  }
}
