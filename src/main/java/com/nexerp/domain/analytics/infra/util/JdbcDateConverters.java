package com.nexerp.domain.analytics.infra.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

public final class JdbcDateConverters {

  private JdbcDateConverters() {
  }

  public static LocalDate toLocalDate(Timestamp ts) {
    return (ts == null) ? null : ts.toLocalDateTime().toLocalDate();
  }

  public static LocalDate toLocalDate(Date d) {
    return (d == null) ? null : d.toLocalDate();
  }
}
