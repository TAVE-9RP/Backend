package com.nexerp.domain.analytics.infra.storage;

import com.nexerp.domain.analytics.config.AnalyticsExportProperties;
import com.nexerp.domain.analytics.port.StoragePort;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocalTmpStorage implements StoragePort {

  private final AnalyticsExportProperties props;

  @Override
  public void ensureBaseDir() {
    try {
      //로컬 저장 위치 없으면 디렉터리 생성
      Files.createDirectories(Path.of(props.localPath()));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create dir: " + props.localPath(), e);
    }
  }

  // 상세 주소("project--20260101.csv" -> "/Users/me/project/tmp/project--20260101.csv")
  @Override
  public Path resolve(String fileName) {
    return Path.of(props.localPath()).resolve(fileName);
  }
}
