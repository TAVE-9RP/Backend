package com.nexerp.domain.analytics.infra.writer;

import com.nexerp.domain.analytics.port.CsvWriterPort;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class GeneralCsvWriter implements CsvWriterPort {

  @Override
  public long write(OutputStream out, String[] header, Stream<String[]> rows) {
    // OutputStream을 문자열 작성이 가능한 BufferedWriter로 감쌉니다.
    // 인코딩은 가장 범용적인 UTF-8을 명시적으로 사용합니다.
    try (BufferedWriter bw = new BufferedWriter(
      new OutputStreamWriter(out, StandardCharsets.UTF_8));
      rows) { // try-with-resources에 Stream을 넣으면 작업 후 자동으로 close됩니다.

      // 1. 헤더 작성
      bw.write(toCsvLine(header));
      bw.newLine();

      // 2. 데이터 작성
      long count = 0;
      var iterator = rows.iterator();

      while (iterator.hasNext()) {
        String[] row = iterator.next();
        bw.write(toCsvLine(row));
        bw.newLine();
        count++;
      }

      // 3. 버퍼의 내용을 출력 스트림으로 밀어냅니다.
      bw.flush();
      return count;

    } catch (IOException e) {
      // 이제 특정 경로(Path)를 알 수 없으므로 범용적인 에러 메시지를 사용합니다.
      throw new IllegalStateException("CSV writing to stream failed", e);
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
