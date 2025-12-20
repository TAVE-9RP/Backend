package com.nexerp.domain.inventory.repository;

import com.nexerp.domain.inventory.model.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
