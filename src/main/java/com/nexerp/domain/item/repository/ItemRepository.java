package com.nexerp.domain.item.repository;

import com.nexerp.domain.item.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
  boolean existsByCode(String code);

  List<Item> findAllByCompanyId(Long companyId);
  @Query("""
      SELECT i
      FROM Item i
      WHERE i.companyId = :companyId
        AND (
        i.code LIKE %:keyword%
        OR i.name LIKE %:keyword%
        OR i.location LIKE %:keyword%
        )
    """)
  List<Item> searchByKeywordAndCompanyId(@Param("keyword") String keyword, @Param("companyId")Long companyId);
}
