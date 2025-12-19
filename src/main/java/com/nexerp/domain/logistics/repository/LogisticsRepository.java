package com.nexerp.domain.logistics.repository;

import com.nexerp.domain.logistics.model.entity.Logistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LogisticsRepository extends JpaRepository<Logistics, Long> {

  @Query("""
    SELECT l
    FROM Logistics l
    JOIN FETCH l.project p
    JOIN FETCH p.company c
    WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithProjectAndCompanyById(Long logisticsId);

  @Query("""
        SELECT DISTINCT l FROM Logistics l
        JOIN FETCH l.logisticsItems li
        JOIN FETCH li.item i
        WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithItemsById(Long logisticsId);
}
