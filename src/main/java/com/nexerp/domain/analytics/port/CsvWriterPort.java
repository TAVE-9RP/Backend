package com.nexerp.domain.analytics.port;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface CsvWriterPort {

  //총 몇 개의 행(Row)을 파일에 기록
  long write(Path path, String[] header, Stream<String[]> rows);
}
