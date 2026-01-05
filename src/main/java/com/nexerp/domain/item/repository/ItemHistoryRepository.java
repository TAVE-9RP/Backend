package com.nexerp.domain.item.repository;

import com.nexerp.domain.item.model.entity.ItemHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemHistoryRepository extends JpaRepository<ItemHistory, Long> {

}
