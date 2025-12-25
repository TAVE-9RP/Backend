package com.nexerp.domain.item.model.entity;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import com.nexerp.domain.logisticsItem.model.entity.LogisticsItem;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "item_id")
  private Long id;

  @Column(name = "item_code", nullable = false, unique = true)
  private String code;

  @Column(name = "item_name", nullable = false)
  private String name;

  @Column(name = "item_price")
  private Long price;

  @Column(name = "item_quantity")
  private Long quantity;

  @Column(name = "item_received_at")
  private LocalDateTime receivedAt;

  @Column(name = "item_location")
  private String location;

  @Column(name = "item_created_at")
  private LocalDateTime createdAt;

  @Column(name = "safety_stock")
  private Long safetyStock;

  @Column(name = "target_stock")
  private Long targetStock;

  @Column(name = "company_id", nullable = false)
  private Long companyId;

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InventoryItem> inventoryItems = new ArrayList<>();

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
  private List<LogisticsItem> logisticsItems = new ArrayList<>();

  @Builder
  public Item(
      Long companyId,
      String code,
      String name,
      Long price,
      Long quantity,
      LocalDateTime receivedAt,
      String location,
      LocalDateTime createdAt,
      Long safetyStock,
      Long targetStock) {
    this.companyId = companyId;
    this.code = code;
    this.name = name;
    this.price = price;
    this.quantity = quantity;
    this.receivedAt = receivedAt;
    this.location = location;
    this.createdAt = createdAt;
    this.safetyStock = safetyStock;
    this.targetStock = targetStock;
  }

  public void increaseQuantity(Long addedQuantity) {
    this.quantity += addedQuantity;
    // 최근 입고일 업데이트
    this.receivedAt = LocalDateTime.now();
  }

  public void decreaseQuantity(Long removedQuantity) {

    if (this.quantity < removedQuantity) {
      throw new BaseException(GlobalErrorCode.STATE_CONFLICT, "출하 요청 수량이 현재 재고를 넘고 있습니다.");
    }

    this.quantity -= removedQuantity;
  }
}
