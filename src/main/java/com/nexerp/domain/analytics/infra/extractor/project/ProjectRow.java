package com.nexerp.domain.analytics.infra.extractor.project;

import java.time.LocalDate;

public record ProjectRow(
  long projectId,
  long companyId,
  String status,
  LocalDate createdAt,
  LocalDate endDate,
  LocalDate expectedEndDate
) {

  public String[] toCsvArray(LocalDate exportDate) {
    return new String[]{
      exportDate.toString(),
      String.valueOf(projectId),
      String.valueOf(companyId),
      status != null ? status : "",
      createdAt != null ? createdAt.toString() : "",
      endDate != null ? endDate.toString() : "",
      expectedEndDate != null ? expectedEndDate.toString() : ""
    };
  }
}
