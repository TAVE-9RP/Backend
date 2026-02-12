package com.nexerp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator;
import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator.ExportResult;
import com.nexerp.domain.analytics.config.AnalyticsExportProperties;
import com.nexerp.domain.analytics.domain.ExportFileName;
import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.infra.storage.LocalTmpStorage;
import com.nexerp.domain.analytics.infra.storage.S3Storage;
import com.nexerp.domain.analytics.port.StoragePort;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@ActiveProfiles("local")
// 저장 경로 지정
@TestPropertySource(properties = {
  "analytics.export.local-path=build/tmp-test"
})
@SpringBootTest(classes = NexerpApplication.class)
@EnableAutoConfiguration(exclude = {BatchAutoConfiguration.class})
class AnalyticsExportIntegrationTest {

  @Autowired
  AnalyticsExportOrchestrator orchestrator;
  Path generated;
  @Autowired
  private StoragePort storage;

  @Autowired
  private LocalTmpStorage localStorage;
  @Autowired
  private S3Storage s3Storage;

  @Autowired
  private AnalyticsExportProperties props;
  @Autowired
  private S3Client s3Client;

  private Map<ExportTable, ExportResult> lastResults;

  // 운영 S3에는 로컬 DB의 테스트 CSV가 적재되지 않도록 AfterEach를 통해 제거
  @AfterEach
  void cleanup() {
    if (lastResults != null) {
      lastResults.values().forEach(r -> {
        String fileName = ExportFileName.of(r.table().filePrefix(), r.date()).toFileName();
        String key = s3Storage.resolve(fileName);
        try {
          s3Storage.deleteIfExists(key);
        } catch (Exception ignored) {
        }
      });
    }

    // Local cleanup: 남아있을 수 있는 파일 제거(성공 시엔 원래 없어야 정상)
    try {
      Files.createDirectories(Path.of(props.localPath()));
      Files.list(Path.of(props.localPath())).forEach(p -> {
        try {
          Files.deleteIfExists(p);
        } catch (Exception ignored) {
        }
      });
    } catch (Exception ignored) {
    }
  }

  @Test
  void all_exports_S3_upload_test() throws IOException {
    // Given
    LocalDate testDate = LocalDate.now().minusDays(1);

    // When
    Map<ExportTable, ExportResult> results = assertDoesNotThrow(() ->
      orchestrator.exportAllFailFastParallel(testDate)
    );

    // S3에 실제로 파일이 올라갔는지 storagePort(S3Storage)를 통해 확인
    assertThat(results).isNotEmpty();

    for (ExportResult result : results.values()) {
      String fileName = ExportFileName.of(result.table().filePrefix(), result.date()).toFileName();
      String s3Key = storage.resolve(fileName); // S3용 Key 경로 생성

      List<String> s3Files = storage.listBaseFiles();

      assertThat(s3Files)
        .as("S3 버킷에 파일이 존재하지 않습니다: %s", fileName)
        .contains(fileName);

      System.out.println("[S3 Verification Success] Found Key: " + s3Key);
    }
  }


  @Test
  @DisplayName("S3 업로드 완료 -> S3의 CSV 행 수, ExportResult.rowCount 일치 -> 성공 후 로컬 파일을 삭제")
  void export_all_tables_parallel_uploads_to_s3_and_cleans_local() {
    // Given
    LocalDate targetDate = LocalDate.now().minusDays(1);

    // When
    lastResults = assertDoesNotThrow(() -> orchestrator.exportAllFailFastParallel(targetDate));

    // Then
    assertThat(lastResults).isNotEmpty();

    // S3에 실제 업로드 되었는지 + 내용(행수) 검증 + 로컬 cleanup 검증
    for (ExportResult result : lastResults.values()) {
      String fileName = ExportFileName.of(result.table().filePrefix(), result.date()).toFileName();

      // 1) 로컬 파일은 성공 후 삭제되어야 함
      Path localFinalPath = Path.of(localStorage.resolve(fileName));
      assertThat(Files.exists(localFinalPath))
        .as("성공 후 로컬 파일이 남아있으면 안 됩니다: %s", localFinalPath)
        .isFalse();

      // 2) S3 객체 존재 + 내용(행수) 검증
      String key = s3Storage.resolve(fileName);

      String body = s3Client.getObjectAsBytes(GetObjectRequest.builder()
          .bucket(props.s3Bucket())
          .key(key)
          .build())
        .asUtf8String();

      long lineCount = countLines(body); // header 포함 라인수
      assertThat(lineCount)
        .as("CSV는 최소 헤더 1줄이 있어야 합니다. table=%s key=%s", result.table(), key)
        .isGreaterThanOrEqualTo(1);

      long dataRowCount = lineCount - 1; // 헤더 제외
      assertThat(dataRowCount)
        .as("S3 CSV 행 수와 ExportResult.rowCount 불일치. table=%s key=%s", result.table(), key)
        .isEqualTo(result.rowCount());
    }
  }

  @Test
  @DisplayName("S3 업로드 결과가 listBaseFiles에 반영된다(파일명 기준)")
  void all_exports_s3_list_contains_files() throws IOException {
    LocalDate testDate = LocalDate.now().minusDays(1);

    lastResults = assertDoesNotThrow(() -> orchestrator.exportAllFailFastParallel(testDate));
    assertThat(lastResults).isNotEmpty();

    // S3 prefix 아래 파일명 리스트에 포함되는지 확인
    var s3Files = s3Storage.listBaseFiles();

    for (ExportResult result : lastResults.values()) {
      String fileName = ExportFileName.of(result.table().filePrefix(), result.date()).toFileName();
      assertThat(s3Files)
        .as("S3 prefix 아래 파일이 존재하지 않습니다: %s", fileName)
        .contains(fileName);
    }
  }

  private static long countLines(String text) {
    try (BufferedReader br = new BufferedReader(new StringReader(text))) {
      return br.lines().count();
    } catch (IOException e) {
      // StringReader는 IOException 거의 안 나지만 시그니처 맞추기
      throw new IllegalStateException(e);
    }
  }

}
