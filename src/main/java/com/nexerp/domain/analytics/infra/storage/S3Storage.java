package com.nexerp.domain.analytics.infra.storage;

import com.nexerp.domain.analytics.config.AnalyticsExportProperties;
import com.nexerp.domain.analytics.port.StoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary // 테스트 시 S3 우선 사용 목적
@RequiredArgsConstructor
public class S3Storage implements StoragePort {

  private final S3Client s3Client;
  private final AnalyticsExportProperties props;

  @Override
  public void ensureBaseDir() {
    log.info("[S3Storage] Base bucket: {}", props.s3Bucket());
  }

  @Override
  public String resolve(String fileNmae) {
    // S3 내의 경로 반환
    // 예: analytics/exports/inventory--2026-01-05.csv
    return props.s3KeyPrefix() + "/" + fileNmae;
  }

  @Override
  public String resolveTemp(String finalPath) {
    return finalPath + ".tmp-" + UUID.randomUUID();
  }

  @Override
  public OutputStream openOutputStream(String fullPath) throws IOException {
    return new ByteArrayOutputStream() {
      @Override
      public void close() throws IOException {
        super.close();
        byte[] bytes = toByteArray();
        s3Client.putObject(PutObjectRequest.builder()
          .bucket(props.s3Bucket())
          .key(fullPath)
          .build(), RequestBody.fromBytes(bytes));
        log.info("[S3Storage] Uploaded to S3: {}", fullPath);
      }
    };
  }

  @Override
  public void moveAtomic(String sourceFullPath, String targetFullPath) throws IOException {
    try {
      CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(props.s3Bucket())
        .sourceKey(sourceFullPath)
        .destinationBucket(props.s3Bucket())
        .destinationKey(targetFullPath)
        .build();
      s3Client.copyObject(copyRequest);

      // 원본 삭제
      deleteIfExists(sourceFullPath);

      log.info("[S3Storage] Moved {} to {}", sourceFullPath, targetFullPath);
    } catch (S3Exception e) {
      throw new IOException("Failed to move S3 object", e);
    }
  }

  @Override
  public void deleteIfExists(String fullPath) {
    try {
      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(props.s3Bucket())
        .key(fullPath)
        .build();
      s3Client.deleteObject(deleteRequest);
    } catch (S3Exception e) {
      log.warn("[S3Storage] Failed to delete S3 object: {}", fullPath);
    }
  }

  @Override
  public List<String> listBaseFiles() {
    try {
      ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
        .bucket(props.s3Bucket())
        .prefix(props.s3KeyPrefix() + "/")
        .build();

      ListObjectsV2Response result = s3Client.listObjectsV2(listRequest);

      return result.contents().stream()
        .map(S3Object::key)
        // 폴더(Prefix) 자체는 제외하고 파일명만 추출
        .filter(key -> !key.endsWith("/"))
        .map(key -> key.substring(key.lastIndexOf("/") + 1))
        .collect(Collectors.toList());
    } catch (S3Exception e) {
      throw new IllegalStateException("Failed to list S3 objects", e);
    }
  }


}
