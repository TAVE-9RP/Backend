package com.nexerp.domain.projectmember.model.entity;

import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.project.model.entity.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_member_id")
  private Long id;

  // 회원 N:1
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  // 프로젝트 N:1
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  public static ProjectMember create(Project project, Member member) {
    ProjectMember pm = new ProjectMember();
    pm.project = project;
    pm.member = member;
    return pm;
  }
}
