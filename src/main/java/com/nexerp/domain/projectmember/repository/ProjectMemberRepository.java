package com.nexerp.domain.projectmember.repository;

import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

}
