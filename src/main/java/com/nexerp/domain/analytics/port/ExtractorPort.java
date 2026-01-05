package com.nexerp.domain.analytics.port;

import com.nexerp.domain.analytics.domain.ExportTable;
import java.time.LocalDate;
import java.util.stream.Stream;

public interface ExtractorPort {

  //어떤 테이블(예: Project, Inventory)을 담당
  ExportTable table();

  //CSV 파일의 맨 윗줄에 들어갈 컬럼 이름
  String[] header();

  //실제 DB 레코드들을 문자열 배열(String[]) 형태로 한 줄씩 가져옴
  Stream<String[]> extractRows(LocalDate date);
  
}
