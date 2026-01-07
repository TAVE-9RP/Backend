package com.nexerp.global.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MainDataSourceConfig {

  @Bean(name = "mainDataSourceProperties")
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSourceProperties mainDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(name = "dataSource")
  @Primary
  public DataSource dataSource(
    @Qualifier("mainDataSourceProperties") DataSourceProperties props
  ) {
    return props.initializeDataSourceBuilder().build();
  }
}
