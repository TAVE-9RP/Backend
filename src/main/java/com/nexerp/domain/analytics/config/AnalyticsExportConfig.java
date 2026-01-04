package com.nexerp.domain.analytics.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AnalyticsExportProperties.class)
public class AnalyticsExportConfig {

}
