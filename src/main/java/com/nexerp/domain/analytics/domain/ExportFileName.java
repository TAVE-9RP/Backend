package com.nexerp.domain.analytics.domain;

import java.time.LocalDate;

// CSV 파일 이름 규칙
public record ExportFileName(String tableName, LocalDate date) {

  public static ExportFileName of(String tableName, LocalDate date) {
    return new ExportFileName(tableName, date);
  }

  public String toFileName() {
    return tableName + "--" + date + ".csv";
  }

  // 삭제 스케줄러(매월 1일에 2개월 전 삭제) 만들 때 사용
  public static ExportFileName parse(String fileName) {

    // csv 파일 아니면 발생
    if (!fileName.endsWith(".csv")) {
      throw new IllegalArgumentException("Not csv: " + fileName);
    }

    // 이름의 .csv 제거
    String base = fileName.substring(0, fileName.length() - 4);

    // 구분자 -- 찾기
    int idx = base.lastIndexOf("--");
    if (idx < 0) {
      throw new IllegalArgumentException("Invalid format: " + fileName);
    }

    //테이블명/날짜 문자열 분리
    String table = base.substring(0, idx);
    String dateStr = base.substring(idx + 2);
    return new ExportFileName(table, LocalDate.parse(dateStr));
  }
}
