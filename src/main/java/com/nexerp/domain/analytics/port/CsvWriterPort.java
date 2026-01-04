package com.nexerp.domain.analytics.port;

import java.io.OutputStream;
import java.util.stream.Stream;

public interface CsvWriterPort {

  //총 몇 개의 행(Row)을 파일에 기록
  long write(OutputStream out, String[] header, Stream<String[]> rows);
}
