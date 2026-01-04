package com.nexerp.domain.analytics.infra.extractor.item;

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
public class ItemExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;

  @Override
  public ExportTable table() {
    return ExportTable.ITEM;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "item_id",
      "item_quantity",
      "safety_stock"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT item_id,
             item_quantity,
             safety_stock
      FROM item
      ORDER BY item_id
      """;

    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  private ItemRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    // quantity / safety_stock 은 nullable 가능성 있어서 getObject로 안전하게 받는 것도 선택지
    return new ItemRow(
      rs.getLong("item_id"),
      rs.getObject("item_quantity", Long.class),
      rs.getObject("safety_stock", Long.class)
    );
  }
}
