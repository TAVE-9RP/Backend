package com.nexerp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator;
import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator.ExportResult;
import com.nexerp.domain.analytics.domain.ExportFileName;
import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.port.StoragePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

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

//  @AfterEach
//  void cleanup() throws IOException {
//    if (generated != null) {
//      Files.deleteIfExists(generated);
//    }
//  }

  @Test
  @DisplayName("모든 등록된 테이블을 병렬로 추출하고 파일 내용과 행 개수를 검증한다")
  void export_all_tables_parallel_verifies_content() throws IOException {
    // Given
    System.out.println("### ALL EXPORT TEST START ###");
    LocalDate targetDate = LocalDate.now();

    // When
    // (1) 병렬 추출 실행
    Map<ExportTable, ExportResult> results = orchestrator.exportAllFailFastParallel(targetDate);

    // Then
    assertThat(results).isNotEmpty();
    System.out.println("Extracted Tables Count: " + results.size());

    for (ExportResult result : results.values()) {
      // (2) ExportResult에 path가 없으므로 storage를 통해 파일 경로를 다시 계산합니다.
      String fileName = ExportFileName.of(result.table().filePrefix(), result.date()).toFileName();
      String fullPathStr = storage.resolve(fileName);

      // 4. 검증을 위해 Path 객체로 변환합니다.
      Path generatedPath = Path.of(fullPathStr);

      // 1. 파일이 실제로 존재하는지 확인
      assertThat(Files.exists(generatedPath))
        .as("파일이 생성되지 않았습니다: %s", generatedPath)
        .isTrue();

      // 2. 파일 내용 읽기
      List<String> lines = Files.readAllLines(generatedPath);

      // 3. 최소한 헤더(1줄)는 존재해야 함
      assertThat(lines.size())
        .as("%s 테이블의 파일에 데이터가 부족합니다", result.table())
        .isGreaterThanOrEqualTo(1);

      // 4. (파일 줄 수 - 1)이 결과 리포트의 rowCount와 일치하는지 확인
      long fileDataRowCount = lines.size() - 1;
      assertThat(fileDataRowCount)
        .as("%s 테이블의 DB 추출 건수와 파일 기록 건수가 다릅니다", result.table())
        .isEqualTo(result.rowCount());

      System.out.printf("[%s] 파일명: %s, 건수: %d%n",
        result.table(), generatedPath.getFileName(), result.rowCount());
    }

    System.out.println("### ALL EXPORT TEST SUCCESS ###");
  }

  @Test
  void all_exports_S3_upload_test() throws IOException{
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
}
