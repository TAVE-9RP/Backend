package com.nexerp.domain.item.repository;

import com.nexerp.domain.item.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
  boolean existsByCode(String code);
}
