package com.nexerp.batch.ext.repository;

import com.nexerp.batch.ext.dto.ItemRawRow;
import com.nexerp.domain.item.model.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BatchRepository extends JpaRepository<Item, Long> {

  @Query("""
      select new com.nexerp.batch.ext.dto.ItemRawRow(
        i.id, i.companyId, i.code, i.name, i.quantity, i.safetyStock
      )
      from Item i
    """)
  Page<ItemRawRow> findItemRawRows(Pageable pageable);
}
