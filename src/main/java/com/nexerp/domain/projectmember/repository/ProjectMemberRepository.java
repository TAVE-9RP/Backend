package com.nexerp.domain.projectmember.repository;

import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
  // 담당자 검증
  boolean existsByProjectIdAndMemberId(Long projectId, Long memberId);

  List<ProjectMember> findAllByProjectId(Long projectId);
}
