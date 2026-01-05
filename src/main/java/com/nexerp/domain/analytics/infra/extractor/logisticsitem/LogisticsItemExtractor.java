package com.nexerp.domain.analytics.infra.extractor.logisticsitem;

import com.nexerp.domain.analytics.domain.ExportTable;
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
public class LogisticsItemExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;

  @Override
  public ExportTable table() {
    return ExportTable.LOGISTICS_ITEM;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "logistics_item_id",
      "item_id",
      "logistics_id",
      "logistics_processed_quantity"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT logistics_item_id,
             item_id,
             logistics_id,
             logistics_processed_quantity
      FROM logistics_item
      ORDER BY logistics_item_id
      """;

    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  private LogisticsItemRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    return new LogisticsItemRow(
      rs.getLong("logistics_item_id"),
      rs.getLong("item_id"),
      rs.getLong("logistics_id"),
      rs.getLong("logistics_processed_quantity")
    );
  }
}
