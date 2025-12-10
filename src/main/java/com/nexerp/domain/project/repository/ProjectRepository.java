package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  boolean existsByNumber(String number);

  @Query("""
    SELECT p.id
    FROM Project p
    WHERE p.company.id = :companyId
      AND (p.title LIKE CONCAT('%', :keyword, '%')
           OR p.number LIKE CONCAT('%', :keyword, '%'))
    ORDER BY p.createDate DESC
    """)
  List<Long> findProjectIds(
    @Param("companyId") Long companyId,
    @Param("keyword") String keyword
  );

  @Query("""
    SELECT DISTINCT p
    FROM Project p
    JOIN FETCH p.company c
    LEFT JOIN FETCH p.projectMembers pm
    LEFT JOIN FETCH pm.member m
    WHERE p.id IN :ids
    ORDER BY p.createDate DESC
    """)
  List<Project> findProjectsWithMembers(@Param("ids") List<Long> ids);


  @Query("SELECT p FROM Project p "
    + "JOIN FETCH p.company c "
    + "LEFT JOIN FETCH p.projectMembers pm "
    + "LEFT JOIN FETCH pm.member m "
    + "WHERE p.id = :projectId")
  Optional<Project> findProjectDetailsById(Long projectId);
}
