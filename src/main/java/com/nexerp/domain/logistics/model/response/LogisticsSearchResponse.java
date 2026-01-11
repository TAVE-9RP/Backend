package com.nexerp.domain.logistics.model.response;

import com.nexerp.domain.logistics.model.entity.Logistics;
import com.nexerp.domain.project.model.entity.Project;
import com.nexerp.global.common.model.TaskStatus;
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

  private final TaskStatus logisticsStatus;

  public static LogisticsSearchResponse from(Project project, List<String> memberNames) {
    Logistics logistics = project.getLogistics();
    if (logistics == null) {
      return null;
    }

    String assigneeSummary;
    if (memberNames == null || memberNames.isEmpty()) {
      assigneeSummary = "-";
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

}
