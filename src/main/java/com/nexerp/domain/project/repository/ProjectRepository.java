package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import java.util.Optional;
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

//  @Query("""
//    SELECT DISTINCT p
//    FROM Project p
//    JOIN FETCH p.company c
//    LEFT JOIN FETCH p.projectMembers pm
//    LEFT JOIN FETCH pm.member m
//    WHERE p.company.id = :companyId
//    ORDER BY p.createDate DESC
//    """)
//  List<Project> findProjectsByCompanyId(@Param("companyId") Long companyId);

  @Query("""
        SELECT DISTINCT p
        FROM Project p
        JOIN FETCH p.company c
        LEFT JOIN FETCH p.projectMembers pm
        LEFT JOIN FETCH pm.member m
        WHERE p.company.id = :companyId
          AND EXISTS (
              SELECT 1
              FROM ProjectMember pm2
              WHERE pm2.project = p
                AND pm2.member.id = :memberId
          )
        ORDER BY p.createDate DESC
    """)
  List<Project> findProjectsByMemberId(@Param("memberId") Long memberId, Long companyId);


}
