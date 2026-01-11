package com.nexerp.domain.projectmember.repository;

import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

  // 담당자 검증
  boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

  List<ProjectMember> findAllByProjectId(Long projectId);


  @Query("""
      select pm
      from ProjectMember pm
        join fetch pm.member m
      where pm.project.id in :projectIds
    """)
  List<ProjectMember> findAllByProjectIdInWithMember(List<Long> projectIds);
}
