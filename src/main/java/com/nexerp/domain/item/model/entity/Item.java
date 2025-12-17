package com.nexerp.domain.item.model.entity;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

  @Column(name = "item_unit_of_measure")
  private String unitOfMeasure;

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

  @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InventoryItem> inventoryItems = new ArrayList<>();

  @Builder
  public Item(String code,
              String name,
              Long price,
              String unitOfMeasure,
              Long quantity,
              LocalDateTime receivedAt,
              String location,
              LocalDateTime createdAt,
              Long safetyStock,
              Long targetStock) {
    this.code = code;
    this.name = name;
    this.price = price;
    this.unitOfMeasure = unitOfMeasure;
    this.quantity = quantity;
    this.receivedAt = receivedAt;
    this.location = location;
    this.createdAt = createdAt;
    this.safetyStock = safetyStock;
    this.targetStock = targetStock;
  }
}
