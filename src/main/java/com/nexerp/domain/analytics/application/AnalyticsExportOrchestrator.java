package com.nexerp.domain.analytics.application;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import com.nexerp.domain.analytics.domain.ExportFileName;
import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.port.CsvWriterPort;
import com.nexerp.domain.analytics.port.ExtractorPort;
import com.nexerp.domain.analytics.port.StoragePort;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsExportOrchestrator {

  private final List<ExtractorPort> extractors;
  private final StoragePort storage;
  private final CsvWriterPort writer;
  private final Executor analyticsExportExecutor;

  /**
   * [Fail-Fast 병렬 내보내기] 1. 하나라도 실패하면 즉시 전체 작업을 중단합니다. 2. 실패 시 이미 성공하여 생성된 파일들도 모두 삭제(Cleanup)합니다.
   * 3. 원자적 파일 생성을 위해 임시 파일(tmp)에 먼저 쓰고 성공 시 최종 위치로 이동합니다.
   */
  public Map<ExportTable, ExportResult> exportAllFailFastParallel(LocalDate date) {
    // 파일을 저장할 폴더가 있는지 확인하고 없으면 만들기
    storage.ensureBaseDir();

    long allStart = System.nanoTime();
    log.info("[AnalyticsExport] Fail-Fast Parallel Export Start");

    Map<ExportTable, ExportResult> results = Collections.synchronizedMap(
      new EnumMap<>(ExportTable.class));

    // 작성된 파일 이름 리스트 (CopyOnWriteArrayList)
    List<String> createdFinalFiles = new CopyOnWriteArrayList<>();

    CompletableFuture<Void> firstFailure = new CompletableFuture<>();
    // completeExceptionally(실패) 를 한번만 전파하기 위한 원자적 블리언
    AtomicBoolean failureSignaled = new AtomicBoolean(false);

    List<CompletableFuture<ExportResult>> futures = extractors.stream()
      .map(extractor -> CompletableFuture.supplyAsync(() -> {
        long start = System.nanoTime();

        // 수정된 원자적 파일 생성 로직 호출
        ExportResult result = exportByExtractorAtomic(extractor, date, createdFinalFiles);

        long elapsedMs = NANOSECONDS.toMillis(System.nanoTime() - start);

        // 비동기와 동기 비교를 위한 로그
        log.info("[AnalyticsExport] table={} rows={} elapsedMs={}",
          result.table(), result.rowCount(), elapsedMs);

        return result;

      }, analyticsExportExecutor).whenComplete((r, e) -> {
        if (e != null) {
          // 하나라도 예외 발생 시 firstFailure을 울려 race를 종료시킴
          if (failureSignaled.compareAndSet(false, true)) {
            firstFailure.completeExceptionally(e);
          }
        } else {
          results.put(r.table(), r);
        }
      }))
      .toList();

    // 모든 스레드가 작업을 종료 했는지 검사
    CompletableFuture<Void> allDone = CompletableFuture.allOf(
      futures.toArray(new CompletableFuture[0]));

    // race는 전체 성공 혹은 실패를 의미
    CompletableFuture<Object> race = CompletableFuture.anyOf(allDone, firstFailure);

    try {
      // 전체 성공 시 통과, 하나라도 실패 시 예외가 여기서 터짐
      race.join();

      long allElapsedMs = NANOSECONDS.toMillis(System.nanoTime() - allStart);
      log.info("[AnalyticsExport] 전체 테이블 수={} 총 소요 시간={}", results.size(),
        allElapsedMs);
      return results;

    } catch (CompletionException e) {
      Throwable cause = (e.getCause() != null) ? e.getCause() : e;
      log.error("[AnalyticsExport] FAIL-FAST triggered. Export stopped.", cause);

      //나머지 스레드 모두 중지
      futures.forEach(f -> f.cancel(true));
      //성공했던 파일들도 모두 제거
      cleanupFiles(createdFinalFiles);

      throw new RuntimeException("분석 데이터 내보내기 중 오류가 발생하여 전체 작업을 중단합니다.", cause);
    }
  }

  /**
   * 부분 파일 방지: - tmp 파일에 쓰고 - 성공하면 final 파일로 move - 실패하면 tmp 삭제
   */
  private ExportResult exportByExtractorAtomic(
    ExtractorPort extractor,
    LocalDate date,
    List<String> createdFinalFiles
  ) {
    //final 파일 경로
    String finalFileName = ExportFileName.of(extractor.table().filePrefix(), date).toFileName();
    String finalPath = storage.resolve(finalFileName); // 최종 결과 경로
    String tmpPath = storage.resolveTemp(finalPath);   // 임시 파일 경로

    try (OutputStream os = storage.openOutputStream(tmpPath)) {
      // 실제 쓰기는 임시에
      long rowCount = writer.write(os, extractor.header(), extractor.extractRows(date));

      // 성공하면 최종 파일로 이동
      storage.moveAtomic(tmpPath, finalPath);

      // 최종 파일 생성 기록 (실패 시 cleanup 용도)
      createdFinalFiles.add(finalPath);

      return new ExportResult(extractor.table(), date, rowCount);

    } catch (Exception e) {
      // 실패하면 tmp 파일 삭제
      try {
        storage.deleteIfExists(tmpPath);
      } catch (Exception ignored) {
        // tmp 삭제 실패는 로깅
        log.warn("[AnalyticsExport] tmp 삭제 실패 tmp={}", tmpPath);
      }
      throw new RuntimeException("테이블 추출 실패: table=" + extractor.table(), e);
    }
  }

  /**
   * 실패 시 이미 만들어진 파일들을 정리
   */
  private void cleanupFiles(List<String> createdFinalFiles) {
    for (String path : createdFinalFiles) {
      try {
        storage.deleteIfExists(path);
        log.info("[AnalyticsExport] cleanup deleted file={}", path);
      } catch (Exception e) {
        log.warn("[AnalyticsExport] cleanup failed file={}", path, e);
      }
    }
  }

  public int deleteTwoMonthsAgo(LocalDate now) {
    YearMonth target = YearMonth.from(now.minusMonths(2)); // 1월이면 11월
    AtomicInteger deleted = new AtomicInteger();

    storage.listBaseFiles()
      .forEach(fileName -> {
        ExportFileName parsed;
        try {
          parsed = ExportFileName.parse(fileName);
        } catch (IllegalArgumentException e) {
          return;
        }

        if (YearMonth.from(parsed.date()).equals(target)) {
          String fullName = storage.resolve(fileName).toString();
          storage.deleteIfExists(fullName);
          deleted.incrementAndGet();
        }
      });

    return deleted.get();
  }

  //ExportResult 객체 하나는 데이터베이스의 특정 테이블 하나를 CSV 파일 하나로 추출한 결과
  public record ExportResult(ExportTable table, LocalDate date, long rowCount) {

  }
}
