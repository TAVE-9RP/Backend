package com.nexerp.domain.item.repository;

import com.nexerp.domain.item.model.entity.ItemHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemHistoryRepository extends JpaRepository<ItemHistory, Long> {

  List<ItemHistory> findByItemIdOrderByProcessedAtDesc(Long itemId);
}
