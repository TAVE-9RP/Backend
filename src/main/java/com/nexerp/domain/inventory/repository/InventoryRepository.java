package com.nexerp.domain.inventory.repository;

import com.nexerp.domain.inventory.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
