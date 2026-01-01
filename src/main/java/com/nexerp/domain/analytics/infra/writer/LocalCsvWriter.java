package com.nexerp.domain.analytics.infra.writer;

import com.nexerp.domain.analytics.port.CsvWriterPort;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class LocalCsvWriter implements CsvWriterPort {

  @Override
  public long write(Path path, String[] header, Stream<String[]> rows) {
    try {
      Files.createDirectories(path.getParent());

      try (BufferedWriter bw = Files.newBufferedWriter(path,
        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        rows
      ) {

        // 헤더 작성
        bw.write(toCsvLine(header));
        bw.newLine();

        // 데이터 작성 (Stream을 반복 가능한 Iterator로 변환)
        long count = 0;
        var iterator = rows.iterator(); // Stream에서 한 행씩 추출

        while (iterator.hasNext()) { // 다음 데이터가 있는지 확인
          String[] r = iterator.next();
          bw.write(toCsvLine(r));
          bw.newLine();
          count++;
        }

        bw.flush();
        return count;
      }
    } catch (IOException e) {
      throw new IllegalStateException("CSV write failed: " + path, e);
    }
  }

  private String toCsvLine(String[] cols) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cols.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(escape(cols[i]));
    }
    return sb.toString();
  }

  // CSV 규칙에 맞게 수정
  private String escape(String v) {
    if (v == null) {
      return "";
    }
    // , 존재 > ""로 감싸기
    boolean needQuote = v.contains(",") || v.contains("\"") || v.contains("\n");

    // " 존재 > ""로 대체
    String escaped = v.replace("\"", "\"\"");
    return needQuote ? "\"" + escaped + "\"" : escaped;
  }
}
