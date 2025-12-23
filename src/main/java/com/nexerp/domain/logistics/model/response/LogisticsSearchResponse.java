package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.project.model.entity.Project;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsSearchResponse {

  // 출하 업무 id
  private final Long logisticsId;

  // 출하 업무 제목
  private final String logisticsTitle;

  // 거래처(프로젝트)
  private final String customer;

  // 출하 업무 승인 요청 일
  private final LocalDate requestedAt;

  // 출하 업무 담당자
  private final List<String> projectMembers;

  public static LogisticsSearchResponse from(Project project) {

    Logistics logistics = project.getLogistics();

    if (logistics == null) {
      return null;
    }

    List<String> memberNames = project.getProjectMembers().stream()
      .map(pm -> pm.getMember().getName())
      .toList();

    return LogisticsSearchResponse.builder()
      .logisticsId(logistics.getId())
      .logisticsTitle(logistics.getTitle())
      .customer(project.getCustomer())
      .requestedAt(logistics.getRequestedAt())
      .projectMembers(List.copyOf(memberNames))
      .build();
  }

  public static List<LogisticsSearchResponse> fromList(List<Project> projects) {
    return projects.stream()
      .map(LogisticsSearchResponse::from)
      .filter(r -> r != null)
      .toList();
  }
}
