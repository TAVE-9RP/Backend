package com.nexerp.domain.inventoryitem.repository;

import com.nexerp.domain.inventoryitem.model.entity.InventoryItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

  // 입고 요청용
  boolean existsByInventoryId(Long inventoryId);

  boolean existsByInventoryIdAndItemId(Long inventoryId, Long itemId);

  List<InventoryItem> findAllByInventoryId(Long inventoryId);

  @Query("""
      select ii
      from InventoryItem ii
        join fetch ii.item it
      where ii.inventory.id in :inventoryIds
    """)
  List<InventoryItem> findAllByInventoryIdInWithItem(List<Long> inventoryIds);

}
