package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  boolean existsByNumber(String number);

  @Query("""
      select p
      from Project p
      where p.company.id = :companyId
        and (p.title like concat('%', :keyword, '%')
             or p.number like concat('%', :keyword, '%'))
      order by p.createDate desc
      """)
  List<Project> searchByCompanyIdAndTitleOrNumber(
      Long companyId,
      String keyword);

  @Query("SELECT p FROM Project p "
    + "JOIN FETCH p.company c "
    + "LEFT JOIN FETCH p.projectMembers pm "
    + "LEFT JOIN FETCH pm.member m "
    + "WHERE p.id = :projectId")
  Optional<Project> findProjectDetailsById(Long projectId);
}
