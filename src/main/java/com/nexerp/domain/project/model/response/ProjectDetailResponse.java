package com.nexerp.domain.project.model.response;

import com.nexerp.domain.projectmember.model.entity.ProjectMember;
import com.nexerp.domain.projectmember.model.response.MemberIdNameResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ProjectDetailResponse {

  private final Long companyId;

  private final String number;

  private final String name;

  private final String description;

  private final String customer;

  private final LocalDate expectedEndDate;

  private final LocalDateTime endDate;

  private final LocalDateTime createDate;

  private final List<MemberIdNameResponseDto> projectMembers;
}
