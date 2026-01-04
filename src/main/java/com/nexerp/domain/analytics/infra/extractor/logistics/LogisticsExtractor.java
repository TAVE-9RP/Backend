package com.nexerp.domain.analytics.infra.extractor.logistics;

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
public class LogisticsExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;

  @Override
  public ExportTable table() {
    return ExportTable.LOGISTICS;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "logistics_id",
      "project_id",
      "logistic_created_at",
      "logistics_status",
      "logistics_completed_at"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT logistics_id,
             project_id,
             logistic_created_at,
             logistics_status,
             logistics_completed_at
      FROM logistics
      ORDER BY logistics_id
      """;

    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  private LogisticsRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    return new LogisticsRow(
      rs.getLong("logistics_id"),
      rs.getLong("project_id"),
      JdbcDateConverters.toLocalDate(rs.getDate("logistic_created_at")),
      rs.getString("logistics_status"),
      JdbcDateConverters.toLocalDate(rs.getTimestamp("logistics_completed_at"))
    );
  }

}
