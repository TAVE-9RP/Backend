package com.nexerp.batch.ext.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3TestService {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  public void uploadTestFile() throws Exception {
    String content = "Hello ETL Test!";
    String key = "test-upload/hello.txt";

    PutObjectRequest request = PutObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .contentType("text/plain")
      .build();

    s3Client.putObject(request, RequestBody.fromString(content));
  }
}
