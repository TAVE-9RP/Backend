package com.nexerp.batch.kpi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexerp.batch.kpi.model.response.SafetyStockKpiDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiIntegrationService {

  private final S3Client s3Client;
  private final KpiSnapshotService kpiSnapshotService;
  private final ObjectMapper objectMapper;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public void syncSafetyStockFromS3(Long companyId, LocalDate date) {
    // 1. S3 경로 구성 (람다의 저장 규칙과 일치)
    String key = String.format("kpi/inventory/company-%d/safety_stock_%s.json",
      companyId, date.toString());

    try {
      // 2. s3에서 파일 가져오기
      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

      ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

      // 3. JSON 파싱
      SafetyStockKpiDto dto = objectMapper.readValue(s3Object, SafetyStockKpiDto.class);

      if (dto.getSnapshotDate() == null) {
        throw new IllegalArgumentException("S3 JSON 데이터에 snapshotDate가 누락되었습니다.");
      }
      // 4. DB insert/update 로직 호출
      kpiSnapshotService.saveSafetyStockResult(
        dto.getCompanyId(),
        LocalDate.parse(dto.getSnapshotDate()),
        dto.getData().getRate()
      );

      log.info("Successfully synced KPI from S3: {}", key);
    } catch (Exception e) {
      log.error("Failed to sync KPI from S3 for key: {}", key, e);
    }
  }
}
