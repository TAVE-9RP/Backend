package com.nexerp.domain.analytics.port;

import java.io.IOException;
import java.util.List;

// 파일이 저장될 공간(물리적 위치)을 관리하는 규칙
public interface StoragePort {

  void ensureBaseDir();

  String resolve(String fileName);

  String resolveTemp(String finalPath);

  void moveAtomic(String tmpPath, String finalPath) throws IOException;

  void deleteIfExists(String tmpPath);

  List<String> listBaseFiles();
}
