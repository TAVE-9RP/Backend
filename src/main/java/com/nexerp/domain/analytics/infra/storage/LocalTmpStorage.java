package com.nexerp.domain.analytics.infra.storage;

import com.nexerp.domain.analytics.config.AnalyticsExportProperties;
import com.nexerp.domain.analytics.port.StoragePort;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTmpStorage implements StoragePort {

  private final AnalyticsExportProperties props;

  @Override
  public void ensureBaseDir() {
    try {
      Files.createDirectories(Path.of(props.localPath()));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create dir: " + props.localPath(), e);
    }
  }

  /**
   * 전체 경로(Full Path)로 변경
   */
  @Override
  public String resolve(String fileName) {
    return Path.of(props.localPath()).resolve(fileName).toString();
  }

  /**
   * 임시 파일 경로 생성 원본 파일과 같은 디렉터리에 .tmp 접미사를 붙여 생성합니다.
   */
  @Override
  public String resolveTemp(String fileName) {
    // 혹시 남은 기존 쓰레기 파일과 충돌 방지 UUID 도입
    return fileName + ".tmp-" + UUID.randomUUID();
  }

  /**
   * 특정 경로의 파일 쓰기를 위한 OutputStream 열기
   */
  public OutputStream openOutputStream(String fullPath) throws IOException {
    Path path = Path.of(fullPath);
    // 부모 디렉터리가 혹시 없다면 생성
    Files.createDirectories(path.getParent());
    return Files.newOutputStream(path,
      //없으면 새로 만듦
      StandardOpenOption.CREATE,
      //이미 파일이 있다면 내용을 싹 비우고 새로 쓰기
      StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * 파일을 원자적으로 이동 (tmp -> final)
   */
  @Override
  public void moveAtomic(String sourceFullPath, String targetFullPath) throws IOException {
    Files.move(
      Path.of(sourceFullPath),
      Path.of(targetFullPath),
      //대상 파일이 이미 존재한다면 그것을 지우고 덮어쓰겠다
      StandardCopyOption.REPLACE_EXISTING
    );
  }

  /**
   * 파일 삭제
   */
  @Override
  public void deleteIfExists(String fullPath) {
    try {
      Files.deleteIfExists(Path.of(fullPath));
    } catch (IOException e) {
      log.warn("[LocalTmpStorage] Failed to delete file: {}", fullPath);
    }
  }

  @Override
  public List<String> listBaseFiles() {
    Path baseDir = Path.of(props.localPath());

    try {
      Files.createDirectories(baseDir);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create dir: " + baseDir, e);
    }

    // 파일명만 반환
    List<String> result = new ArrayList<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir)) {
      for (Path p : stream) {
        if (Files.isRegularFile(p)) {
          result.add(p.getFileName().toString());
        }
      }
      return result;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to list base files: " + baseDir, e);
    }
  }
}
