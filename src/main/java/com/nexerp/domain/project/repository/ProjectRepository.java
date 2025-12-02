package com.nexerp.domain.project.repository;

import com.nexerp.domain.project.model.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

  boolean existsByNumber(String number);
}
