package com.nexerp.domain.kpi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexerp.domain.kpi.model.response.IntegratedKpiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

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

  public void syncIntegratedKpiFromS3(Long companyId, LocalDate date) {
    // 1. S3 경로 구성 (람다의 저장 규칙과 일치)
    String key = String.format("kpi/daily-report/company-%d/report_%s.json",
      companyId, date.toString());

    try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
      GetObjectRequest.builder().bucket(bucket).key(key).build())) {

      // 2. 통합 JSON 파싱
      IntegratedKpiResponse response = objectMapper.readValue(s3Object, IntegratedKpiResponse.class);

      // 3. DB 저장 로직 호출
      kpiSnapshotService.saveIntegratedResult(response);

      log.info("Successfully synced Integrated KPI for Company: {}, Date: {}", companyId, date);
    } catch (NoSuchKeyException e) {
      // 파일이 없는 경우: 활동이 없는 날이므로 경고나 에러가 아닌 정보성 로그만 남김
      log.info("No KPI report found in S3 for Company: {} at date: {} (No activity)", companyId, date);
    } catch (Exception e) {
      log.error("Failed to sync Integrated KPI from S3: {}", key, e);
    }
  }
}
