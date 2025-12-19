package com.nexerp.domain.item.repository;

import com.nexerp.domain.item.model.entity.Item;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

  List<Item> findAllById(List<Long> itemIds);

}
