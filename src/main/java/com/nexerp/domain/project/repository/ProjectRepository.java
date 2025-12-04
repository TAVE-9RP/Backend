package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  boolean existsByNumber(String number);

  @Query("""
      select p
      from Project p
      where p.company.id = :companyId
        and (p.name like concat('%', :keyword, '%')
             or p.number like concat('%', :keyword, '%'))
      order by p.createDate desc
      """)
  List<Project> searchByCompanyIdAndNameOrNumber(
      Long companyId,
      String keyword);
}
