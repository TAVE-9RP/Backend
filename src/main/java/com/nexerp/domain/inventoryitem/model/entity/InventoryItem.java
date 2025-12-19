package com.nexerp.domain.inventoryitem.model.entity;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventoryitem.model.enums.InventoryProcessingStatus;
import com.nexerp.domain.item.model.entity.Item;
import jakarta.persistence.*;
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
  private InventoryProcessingStatus status;

  @Builder
  public InventoryItem(Inventory inventory,
                       Item item,
                       Long quantity,
                       Long processed_quantity,
                       InventoryProcessingStatus status) {
    this.inventory = inventory;
    this.item = item;
    this.quantity = quantity; // 목표 입고 수량
    this.processed_quantity = processed_quantity; // 현재 입고된 수량
    this.status = status;
  }
}
