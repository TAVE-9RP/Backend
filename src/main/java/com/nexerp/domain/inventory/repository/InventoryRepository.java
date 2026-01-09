package com.nexerp.domain.inventory.repository;

import com.nexerp.domain.inventory.model.entity.Inventory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

  List<Inventory> findAllByProject_Company_Id(Long companyId);

  @Query("""
      select distinct inv
      from Inventory inv
        join inv.project p
        join p.projectMembers pm
      where p.company.id = :companyId
        and pm.member.id = :memberId
    """)
  List<Inventory> findAllAssignedToMember(Long companyId, Long memberId);
}
