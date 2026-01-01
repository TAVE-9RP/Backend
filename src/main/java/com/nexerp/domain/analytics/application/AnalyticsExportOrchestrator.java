package com.nexerp.domain.analytics.application;

import com.nexerp.domain.analytics.domain.ExportFileName;
import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.port.CsvWriterPort;
import com.nexerp.domain.analytics.port.ExtractorPort;
import com.nexerp.domain.analytics.port.StoragePort;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsExportOrchestrator {

  private final List<ExtractorPort> extractors;
  private final StoragePort storage;
  private final CsvWriterPort writer;

  /**
   * 특정 테이블 1개만 export
   */
  public ExportResult exportOne(ExportTable table, LocalDate date) {
    storage.ensureBaseDir();
    ExtractorPort extractor = findExtractor(table);
    return exportByExtractor(extractor, date);
  }

  /**
   * 등록된 Extractor 전체 export
   */
  public Map<ExportTable, ExportResult> exportAll(LocalDate date) {
    storage.ensureBaseDir();

    Map<ExportTable, ExportResult> results = new EnumMap<>(ExportTable.class);
    for (ExtractorPort extractor : extractors) {
      ExportResult result = exportByExtractor(extractor, date);
      results.put(result.table(), result);
    }
    return results;
  }

  private ExtractorPort findExtractor(ExportTable table) {
    return extractors.stream()
      .filter(e -> e.table() == table)
      .findFirst()
      .orElseThrow(() -> new IllegalStateException(table + " Extractor not registered"));
  }

  private ExportResult exportByExtractor(ExtractorPort extractor, LocalDate date) {
    String fileName = ExportFileName.of(extractor.table().filePrefix(), date).toFileName();
    Path path = storage.resolve(fileName);

    long rowCount = writer.write(path, extractor.header(), extractor.extractRows(date));
    return new ExportResult(extractor.table(), date, path, rowCount);
  }

  public record ExportResult(ExportTable table, LocalDate date, Path path, long rowCount) {

  }
}
