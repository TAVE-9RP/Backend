package com.nexerp.domain.analytics.infra.extractor.project;

import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.infra.util.JdbcDateConverters;
import com.nexerp.domain.analytics.port.ExtractorPort;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;

  @Override
  public ExportTable table() {
    return ExportTable.PROJECT;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "project_id",
      "company_id",
      "project_status",
      "project_create_date"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT project_id, 
             company_id, 
             project_status, 
             project_create_date 
      FROM project 
      ORDER BY project_id
      """;

    // 결과 Stream으로 변환
    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  // 데이터베이스의 한 줄을 객체로 바꾸는 전용 메서드
  private ProjectRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    return new ProjectRow(
      rs.getLong("project_id"),
      rs.getLong("company_id"),
      rs.getString("project_status"),
      JdbcDateConverters.toLocalDate(rs.getTimestamp("project_create_date"))
    );
  }
}
