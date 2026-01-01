package com.nexerp.domain.analytics.infra.extractor.logistics;

import java.time.LocalDate;

public record LogisticsRow(
  long logisticsId,
  long projectId,
  LocalDate requestedAt,
  String status,
  LocalDate completedAt
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(logisticsId),
      String.valueOf(projectId),
      requestedAt != null ? requestedAt.toString() : "",
      status != null ? status : "",
      completedAt != null ? completedAt.toString() : ""
    };
  }
}
