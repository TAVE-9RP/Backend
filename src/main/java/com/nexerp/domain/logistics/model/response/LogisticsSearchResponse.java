package com.nexerp.domain.logistics.model.response;

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
  private List<String> projectMembers;
}
