package com.nexerp.domain.inventory.repository;

import com.nexerp.domain.inventory.model.entity.Inventory;
import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  boolean existsByInventoryIdAndItemId(Long inventoryId, Long itemId);

  List<InventoryItem> findByInventoryId(Long inventoryId);
}
