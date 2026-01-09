package com.nexerp.domain.analytics.infra.extractor.inventoryitem;

import com.nexerp.domain.analytics.domain.ExportTable;
import com.nexerp.domain.analytics.port.ExtractorPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class InventoryItemExtractor implements ExtractorPort {

  private final @Qualifier("readOnlyJdbcTemplate") JdbcTemplate jdbcTemplate;
  @Override
  public ExportTable table() {
    return ExportTable.INVENTORY_ITEM;
  }

  @Override
  public String[] header() {
    return new String[]{
      "date",
      "inventory_item_id",
      "item_id",
      "inventory_id"
    };
  }

  @Override
  public Stream<String[]> extractRows(LocalDate date) {
    String sql = """
      SELECT inventory_item_id,
             item_id,
             inventory_id
      FROM inventory_item
      ORDER BY inventory_item_id
      """;

    return jdbcTemplate.queryForStream(sql, this::mapToRow)
      .map(row -> row.toCsvArray(date));
  }

  private InventoryItemRow mapToRow(ResultSet rs, int rowNum) throws SQLException {
    return new InventoryItemRow(
      rs.getLong("inventory_item_id"),
      rs.getLong("item_id"),
      rs.getLong("inventory_id")
    );
  }

}
