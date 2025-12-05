package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

  @Query("""
    SELECT p
    FROM Project p
    JOIN FETCH p.company c
    LEFT JOIN FETCH p.projectMembers pm
    LEFT JOIN FETCH pm.member m
    WHERE p.company.id = :companyId
      AND (p.name LIKE CONCAT('%', :keyword, '%')
           OR p.number LIKE CONCAT('%', :keyword, '%'))
    ORDER BY p.createDate DESC
    """)
  List<Project> searchByCompanyIdAndNameOrNumber2(
    Long companyId,
    @Param("keyword") String keyword);

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

  // 회사 ID로 회사 이름 조회
  @Query("SELECT c.name FROM Company c WHERE c.id = :companyId")
  Optional<String> findCompanyNameById(@Param("companyId") Long companyId);


  // 신규 프로젝트 번호 생성 시 마지막 숫자 확인용
//  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT SUBSTRING(p.number, LENGTH(:codePrefix) + 1) " // 숫자 부분만 추출
    + "FROM Project p "
    + "WHERE p.company.id = :companyId AND p.number LIKE CONCAT(:codePrefix, '%') "
    + "ORDER BY p.number DESC LIMIT 1")
  Optional<String> findMaxProjectSerialNumber(
    @Param("companyId") Long companyId,
    @Param("codePrefix") String codePrefix);
}
