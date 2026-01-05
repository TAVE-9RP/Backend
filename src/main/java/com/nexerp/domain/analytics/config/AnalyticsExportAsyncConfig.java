package com.nexerp.domain.analytics.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AnalyticsExportAsyncConfig {

  @Bean(name = "analyticsExportExecutor")
  public Executor analyticsExportExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // 기본 스레드 수
    executor.setCorePoolSize(6);
    // 최대 스레드 수
    executor.setMaxPoolSize(8);
    // 대기 작업 큐
    executor.setQueueCapacity(100);
    // 스레드의 이름 접두사
    executor.setThreadNamePrefix("analytics-export-");

    executor.initialize();

    return executor;
  }
}
