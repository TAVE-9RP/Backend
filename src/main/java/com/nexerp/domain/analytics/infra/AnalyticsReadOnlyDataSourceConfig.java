package com.nexerp.domain.analytics.infra;

import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Slf4j
@Configuration
public class AnalyticsReadOnlyDataSourceConfig {

  @Bean(name = "analyticsDataSourceProperties")
  @ConfigurationProperties(prefix = "analytics.datasource")
  public DataSourceProperties analyticsDataSourceProperties() {
    return new DataSourceProperties();
  }

  /**
   * ReadOnly 전용 DataSource 생성 - DataSourceProperties 기반으로 생성하면 url/username/password를 표준적으로 처리 가능
   */
  @Bean(name = "analyticsReadOnlyDataSource")
  public DataSource analyticsReadOnlyDataSource(
    @Qualifier("analyticsDataSourceProperties") DataSourceProperties props
  ) {
    DataSource ds = props.initializeDataSourceBuilder().build();
    log.info("[Analytics-ReadOnly] DataSource initialized. url={}", props.getUrl());
    return ds;
  }

  /**
   * ReadOnly 전용 JdbcTemplate
   */
  @Bean(name = "readOnlyJdbcTemplate")
  public JdbcTemplate readOnlyJdbcTemplate(
    @Qualifier("analyticsReadOnlyDataSource") DataSource ds
  ) {
    return new JdbcTemplate(ds);
  }
}
