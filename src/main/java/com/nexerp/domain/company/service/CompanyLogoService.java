package com.nexerp.domain.company.service;

import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyLogoService {

  private static final long MAX_BYTES = 2 * 1024 * 1024; // 2MB
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/png", "image/jpeg");
  private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg");
  private final S3Client s3Client;

  @Value("${cloud.aws.s3.logo.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.logo.baseurl}")
  private String publicBaseUrl;

  public String buildLogoUrl(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return null;
    }
    String base =
      publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
        : publicBaseUrl;
    return base + "/" + objectKey;
  }

  public String uploadCompanyLogo(Long companyId, MultipartFile file) {

    if (file == null || file.isEmpty()) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일이 비어있습니다.");
    }

    // 확장자 추출
    String rawExt = extractExt(file.getOriginalFilename());
    // 검증
    validateFile(file, rawExt);
    // 가공
    String ext = normalizeExt(rawExt);
    String key = generateKey(companyId, ext);

    //업로드
    PutObjectRequest putReq = PutObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .contentType(file.getContentType())
      // uuid 기반 버전키를 사용해 서버 부하를 줄임
      .cacheControl("public, max-age=31536000, immutable")
      .build();

    try (InputStream is = file.getInputStream()) {
      s3Client.putObject(
        putReq,
        RequestBody.fromInputStream(is, file.getSize())
      );
      log.info("[S3] Uploaded company logo. bucket={}, key={}, size={}", bucket, key,
        file.getSize());
      return key;
    } catch (IOException e) {
      throw new BaseException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "로고 파일을 읽는 중 오류가 발생했습니다.");
    } catch (S3Exception e) {
      log.error("[S3] Upload failed. bucket={}, key={}, awsMessage={}", bucket, key,
        e.awsErrorDetails().errorMessage(), e);
      throw new BaseException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "S3 업로드에 실패했습니다.");
    }
  }

  /**
   * DB 저장 실패 등으로 S3에 올라간 파일을 되돌려야 할 때 사용.
   */
  public void deleteLogoQuietly(String objectKey) {
    if (objectKey == null || objectKey.isBlank()) {
      return;
    }

    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(objectKey)
        .build());
      log.info("[S3] Deleted logo. bucket={}, key={}", bucket, objectKey);
    } catch (S3Exception e) {
      // 보상 삭제는 실패해도 비즈니스 전체를 깨지 않게 "조용히" 처리하고 로그만 남기는 게 보통 유리
      log.warn("[S3] Failed to delete logo. bucket={}, key={}, awsMessage={}",
        bucket, objectKey, e.awsErrorDetails().errorMessage(), e);
    }
  }

  private void validateFile(MultipartFile file, String rawExt) {
    if (file == null || file.isEmpty()) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일이 비어있습니다.");
    }
    if (file.getSize() > MAX_BYTES) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일은 최대 2MB까지 업로드할 수 있습니다.");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일은 PNG 또는 JPG(JPEG)만 허용합니다.");
    }

    if (rawExt == null || !ALLOWED_EXT.contains(rawExt)) {
      throw new BaseException(GlobalErrorCode.BAD_REQUEST, "로고 파일 확장자는 png/jpg/jpeg만 허용합니다.");
    }
  }

  private String generateKey(Long companyId, String ext) {
    String uuid = UUID.randomUUID().toString().replace("-", "");
    return String.format("assets/company-logos/company_%d/%s.%s", companyId, uuid, ext);
  }

  private String normalizeExt(String ext) {
    if (ext.equals("jpeg")) {
      return "jpg";
    }
    return ext;
  }

  // 확장자 추출(소문자)
  private String extractExt(String filename) {
    if (filename == null) {
      return null;
    }
    int dot = filename.lastIndexOf('.');
    if (dot < 0 || dot == filename.length() - 1) {
      return null;
    }
    return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
  }
}
