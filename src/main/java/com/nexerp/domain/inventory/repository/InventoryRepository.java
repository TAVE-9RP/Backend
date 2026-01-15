package com.nexerp.domain.inventory.repository;

import com.nexerp.domain.inventory.model.entity.Inventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  @Query("""
      select inv.id
      from Inventory inv
        join inv.project p
      where p.company.id = :companyId
        and (
          :keyword is null or :keyword = ''
          or inv.title like concat('%', :keyword, '%')
          or p.number like concat('%', :keyword, '%')
        )
      order by inv.createdAt desc
    """)
  List<Long> findInventoryIdsForSearchByCompany(
    Long companyId,
    String keyword
  );

  @Query("""
      select inv.id
      from Inventory inv
        join inv.project p
      where p.company.id = :companyId
        and exists (
          select 1
          from ProjectMember pm2
          where pm2.project = p
            and pm2.member.id = :memberId
        )
        and (
          :keyword = ''
          or inv.title like concat('%', :keyword, '%')
          or p.number like concat('%', :keyword, '%')
        )
      order by inv.createdAt desc
    """)
  List<Long> findInventoryIdsForSearchByCompanyAndMember(
    Long companyId,
    Long memberId,
    String keyword
  );

  @Query("""
      select inv
      from Inventory inv
        join fetch inv.project p
      where inv.id in :inventoryIds
    """)
  List<Inventory> findInventoriesWithProjectByIds(
    List<Long> inventoryIds
  );
}
