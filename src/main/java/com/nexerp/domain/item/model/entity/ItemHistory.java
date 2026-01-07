package com.nexerp.domain.item.model.entity;

import com.nexerp.domain.item.model.enums.TaskType;
import com.nexerp.domain.member.model.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_history")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "item_history_id")
  private Long id;

  // 업무(입고/출하) 대상
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  // Logistics 또는 Inventory
  @Enumerated(EnumType.STRING)
  @Column(name = "item_history_task_type", nullable = false)
  private TaskType taskType;

  // 실제 업무(LogisticsItem/InventoryItem)의 PK
  @Column(name = "source_id", nullable = false)
  private Long sourceId;

  // 업무(입고/출하)를 수행한 담당자
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  //처리일
  @Column(name = "item_history_processing_at", nullable = false)
  private LocalDateTime processedAt;

  // 건당 처리된 수량
  @Column(name = "item_history_change_quantity", nullable = false)
  private Long changeQuantity;

  // 처리 후 수량
  @Column(name = "item_history_remaining_quantity", nullable = false)
  private Long remainingQuantity;

  public static ItemHistory of(Item item, TaskType taskType, Long sourceId, Member member,
    Long changeQuantity,
    Long remainingQuantity) {
    return ItemHistory.builder()
      .item(item)
      .taskType(taskType)
      .sourceId(sourceId)
      .member(member)
      .processedAt(LocalDateTime.now())
      .changeQuantity(changeQuantity)
      .remainingQuantity(remainingQuantity)
      .build();
  }

  public static ItemHistory received(Item item, Long sourceId, Member member, Long changeQuantity,
    Long remainingQuantity) {
    return of(item, TaskType.INVENTORY, sourceId, member, changeQuantity, remainingQuantity);
  }

  public static ItemHistory shipped(Item item, Long sourceId, Member member, Long changeQuantity,
    Long remainingQuantity) {
    return of(item, TaskType.LOGISTICS, sourceId, member, changeQuantity, remainingQuantity);
  }

}
