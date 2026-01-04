package com.nexerp.domain.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "analytics.export")
public record AnalyticsExportProperties(
  // 로컬 저장 위치 파일 명 제외
  String localPath,
  // 제거 주기
  int retentionMonths
) {

}
