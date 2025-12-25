package com.nexerp.domain.inventoryitem.model.entity;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.global.common.model.TaskProcessingStatus;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "inventory_item_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventory_id", nullable = false)
  private Inventory inventory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  // 목표 입고 수량
  @Column(name = "inventory_targeted_quantity")
  private Long quantity;

  // 현재까지 입고된 수량
  @Column(name = "inventory_processed_quantity")
  private Long processed_quantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "inventory_processing_status", nullable = false)
  private TaskProcessingStatus status;

  @Builder
  public InventoryItem(Inventory inventory,
    Item item,
    Long quantity,
    Long processed_quantity,
    TaskProcessingStatus status) {
    this.inventory = inventory;
    this.item = item;
    this.quantity = quantity; // 목표 입고 수량
    this.processed_quantity = processed_quantity; // 현재 입고된 수량
    this.status = status;
  }

  public void updateStatus(TaskProcessingStatus status) {
    this.status = status;
  }

  public void updateProcessedQuantity(Long addedQuantity) {
    this.processed_quantity += addedQuantity;
  }

  public void updateTargetQuantity(Long quantity) {
    this.quantity = quantity;
  }
}
