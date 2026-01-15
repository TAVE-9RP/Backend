package com.nexerp.domain.kpi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiHistoryService {
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public List<Map<String, Object>> getShipmentLeadTimeHistory(Long companyId) {
    String key = String.format("kpi/history/company_%d_history.json", companyId);

    try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(
      GetObjectRequest.builder().bucket(bucket).key(key).build())) {

      return objectMapper.readValue(s3Object, new TypeReference<List<Map<String, Object>>>() {});
    } catch (Exception e) {
      log.warn("과거 이력 데이터가 없습니다. Company: {}", companyId);
      return Collections.emptyList();
    }
  }
}
