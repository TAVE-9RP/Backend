package com.nexerp.domain.project.model.response;

import com.nexerp.domain.member.model.response.MemberIdNameResponseDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectDetailResponse {

  private final String projectNumber;

  private final String projectTitle;

  private final String description;

  private final String customer;

  private final LocalDate expectedEndDate;

  private final LocalDateTime endDate;

  private final LocalDateTime createDate;

  private final List<MemberIdNameResponseDto> projectMembers;
}
