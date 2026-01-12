package com.nexerp.domain.logistics.repository;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.project.model.entity.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LogisticsRepository extends JpaRepository<Logistics, Long> {

  @Query("""
    SELECT DISTINCT l
    FROM Logistics l
    JOIN FETCH l.project p
    JOIN FETCH p.company c
    WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithProjectAndCompanyById(Long logisticsId);

  @Query("""
    SELECT DISTINCT l
    FROM Logistics l
    JOIN FETCH l.project p
    JOIN FETCH p.company c
    LEFT JOIN FETCH p.projectMembers pm
    LEFT JOIN FETCH pm.member m
    WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithProjectCompanyAndMemberById(Long logisticsId);

  @Query("""
    SELECT DISTINCT l
    FROM Logistics l
    JOIN FETCH l.project p
    JOIN FETCH p.company c
    JOIN FETCH l.logisticsItems li
    WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithProjectCompanyAndItemsById(Long logisticsId);

  @Query("""
        SELECT DISTINCT l
        FROM Logistics l
        JOIN FETCH l.project p
        JOIN FETCH p.company c
        JOIN FETCH l.logisticsItems li
        JOIN FETCH li.item i
        WHERE l.id = :logisticsId
    """)
  Optional<Logistics> findWithAllDetailsById(Long logisticsId);

  @Query("""
        select p.id
        from Project p
          join p.logistics l
        where p.company.id = :companyId
          and (
            :keyword is null or :keyword = ''
            or (p.number like concat('%', :keyword, '%')
            or p.title like concat('%', :keyword, '%')
            or l.title like concat('%', :keyword, '%'))
          )
        order by p.createDate desc
    """)
  List<Long> findProjectIdsWithLogisticsByKeyword(
    Long companyId,
    String keyword
  );

  @Query("""
      select p.id
      from Project p
        join p.logistics l
      where p.company.id = :companyId
        and exists (
          select 1
          from ProjectMember pm2
          where pm2.project = p
            and pm2.member.id = :memberId
        )
        and (
          :keyword = ''
          or l.title like concat('%', :keyword, '%')
          or p.number like concat('%', :keyword, '%')
          or p.title like concat('%', :keyword, '%')
        )
      order by p.createDate desc
    """)
  List<Long> findProjectIdsForLogisticsSearchByCompanyAndMember(
    Long companyId,
    Long memberId,
    String keyword
  );

  @Query("""
      select p
      from Project p
        join fetch p.company c
        join fetch p.logistics l
      where p.id in :projectIds
    """)
  List<Project> findProjectsWithLogisticsByIds(List<Long> projectIds);
}
