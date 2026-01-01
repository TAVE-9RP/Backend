package com.nexerp;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator;
import com.nexerp.domain.analytics.application.AnalyticsExportOrchestrator.ExportResult;
import com.nexerp.domain.analytics.domain.ExportTable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("local")
// 저장 경로 지정
@TestPropertySource(properties = {
  "analytics.export.local-path=build/tmp-test"
})
@SpringBootTest(classes = NexerpApplication.class)
class AnalyticsExportIntegrationTest {

  @Autowired
  AnalyticsExportOrchestrator orchestrator;

  Path generated;

//  @AfterEach
//  void cleanup() throws IOException {
//    if (generated != null) {
//      Files.deleteIfExists(generated);
//    }
//  }

  @Test
  @DisplayName("모든 등록된 테이블을 CSV로 추출하고 파일 내용과 DB 행 개수를 검증한다")
  void export_all_tables_creates_csv_files_and_verifies_content() throws IOException {
    // Given
    System.out.println("### ALL EXPORT TEST START ###");
    LocalDate targetDate = LocalDate.now();

    // When
    // 모든 테이블 추출 실행
    Map<ExportTable, ExportResult> results = orchestrator.exportAll(targetDate);

    // Then
    assertThat(results).isNotEmpty(); // 최소한 하나 이상의 추출 결과가 있어야 함
    System.out.println("Extracted Tables Count: " + results.size());

    for (ExportResult result : results.values()) {
      var generatedPath = result.path();

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

      System.out.printf("[%s] 경로: %s, 건수: %d%n",
        result.table(), generatedPath.getFileName(), result.rowCount());
    }

    System.out.println("### ALL EXPORT TEST SUCCESS ###");
  }
}
