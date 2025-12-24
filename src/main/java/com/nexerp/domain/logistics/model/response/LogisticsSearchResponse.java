package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.logistics.model.enums.LogisticsStatus;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LogisticsSearchResponse {

  // 출하 업무 id
  private final Long logisticsId;

  private final String projectNumber;

  // 출하 업무 제목
  private final String logisticsTitle;

  // 거래처(프로젝트)
  private final String customer;

  // 프로젝트 담당자
  private final String assigneeSummary; // 홍길동 외 3명

  // 출하 업무 승인 요청 일
  private final LocalDate requestedAt;

  private final LogisticsStatus logisticsStatus;

  public static LogisticsSearchResponse from(Project project) {

    Logistics logistics = project.getLogistics();

    if (logistics == null) {
      return null;
    }

    List<String> memberNames = project.getProjectMembers().stream()
      .map(pm -> pm.getMember().getName())
      .toList();

    String assigneeSummary;
    if (memberNames.isEmpty()) {
      throw new BaseException(GlobalErrorCode.NOT_FOUND, "프로젝트 담당자를 찾을 수 없습니다.");
    } else if (memberNames.size() == 1) {
      assigneeSummary = memberNames.get(0);
    } else {
      assigneeSummary = memberNames.get(0) + " 외 " + (memberNames.size() - 1) + "명";
    }

    return LogisticsSearchResponse.builder()
      .logisticsId(logistics.getId())
      .projectNumber(project.getNumber())
      .logisticsTitle(logistics.getTitle())
      .customer(project.getCustomer())
      .assigneeSummary(assigneeSummary)
      .requestedAt(logistics.getRequestedAt())
      .logisticsStatus(logistics.getStatus())
      .build();
  }

  public static List<LogisticsSearchResponse> fromList(List<Project> projects) {
    return projects.stream()
      .map(LogisticsSearchResponse::from)
      .filter(r -> r != null)
      .toList();
  }
}
