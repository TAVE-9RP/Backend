package com.nexerp.domain.analytics.port;

import java.nio.file.Path;

// 파일이 저장될 공간(물리적 위치)을 관리하는 규칙
public interface StoragePort {

  void ensureBaseDir();

  Path resolve(String fileName);
}
