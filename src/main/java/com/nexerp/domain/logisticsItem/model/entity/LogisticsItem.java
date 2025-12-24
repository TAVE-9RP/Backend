package com.nexerp.domain.logisticsItem.model.entity;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logisticsItem.model.enums.LogisticsProcessingStatus;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "logistics_item")
@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LogisticsItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "logistics_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "logistics_id", nullable = false)
  private Logistics logistics;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  // 출하량
  @Column(name = "logistics_processed_quantity", nullable = false)
  private Long processedQuantity;

  //목표 출하 수량
  @Column(name = "logistics_targeted_quantity", nullable = false)
  private Long targetedQuantity;

  // 총 판매액
  @Column(name = "logistic_total_price")
  private BigDecimal totalPrice;

  //물품별 처리 상태(PENDING, IN_PROGRESS, COMPLETED)
  @Enumerated(EnumType.STRING)
  @Column(name = "logistics_processing_status", nullable = false)
  private LogisticsProcessingStatus processingStatus;

  // 물품별 출하 완료일
  @Column(name = "logistics_shipout_date")
  private LocalDateTime shipoutDate;

  public static LogisticsItem create(Logistics logistics, Item item) {
    return LogisticsItem.builder()
      .logistics(logistics)
      .item(item)
      .processedQuantity(0L)
      .targetedQuantity(0L)
      .processingStatus(LogisticsProcessingStatus.NOT_STARTED)
      .shipoutDate(null)
      .build();
  }

  public void applyTargetQuantity(Long targetQuantity) {
    if (targetQuantity == null || targetQuantity < 1) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "목표 수량은 1 이상이어야 합니다.");
    }

    Long price = this.item.getPrice();

    if (price == null) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "물품 가격이 없어 총액을 계산할 수 없습니다.");
    }

    this.targetedQuantity = targetQuantity;
    this.totalPrice = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(targetQuantity));
  }

  public void completedLogisticsItem() {
    this.shipoutDate = LocalDateTime.now();
    changeStatus(LogisticsProcessingStatus.COMPLETED);
  }

  public void undoCompletedLogisticsItem() {
    this.shipoutDate = null;
    changeStatus(LogisticsProcessingStatus.IN_PROGRESS);
  }

  public void changeStatus(LogisticsProcessingStatus status) {
    this.processingStatus = status;
  }

  public void changeProcessedQuantity(Long processedQuantity) {
    this.processedQuantity = processedQuantity;
  }

}
