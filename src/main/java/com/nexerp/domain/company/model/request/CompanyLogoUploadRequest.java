package com.nexerp.domain.company.model.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
public class CompanyLogoUploadRequest {

  @NotNull
  @Schema(description = "업로드할 로고 이미지 파일", type = "string", format = "binary")
  private MultipartFile file;
}
