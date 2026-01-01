package com.nexerp.domain.analytics.infra.extractor.inventory;

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
public class InventoryExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;

  @Override
  public ExportTable table() {
    return ExportTable.INVENTORY;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "inventory_id",
      "project_id",
      "inventory_requested_at",
      "inventory_status",
      "inventory_completed_at"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT inventory_id,
             project_id,
             inventory_requested_at,
             inventory_status,
             inventory_completed_at
      FROM inventory
      ORDER BY inventory_id
      """;

    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  private InventoryRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    return new InventoryRow(
      rs.getLong("inventory_id"),
      rs.getLong("project_id"),
      JdbcDateConverters.toLocalDate(rs.getTimestamp("inventory_requested_at")),
      rs.getString("inventory_status"),
      JdbcDateConverters.toLocalDate(rs.getTimestamp("inventory_completed_at"))
    );
  }

}
