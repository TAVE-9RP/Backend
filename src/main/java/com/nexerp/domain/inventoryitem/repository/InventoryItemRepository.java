package com.nexerp.domain.inventoryitem.repository;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}
