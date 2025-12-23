package com.nexerp.domain.inventoryitem.repository;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

  // 입고 요청용
  boolean existsByInventoryId(Long inventoryId);

  boolean existsByInventoryIdAndItemId(Long inventoryId, Long itemId);
  List<InventoryItem> findAllByInventoryId(Long inventoryId);
}
