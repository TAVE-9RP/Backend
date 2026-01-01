package com.nexerp.batch.ext.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class S3UploadTasklet implements Tasklet {

  private final S3Client s3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${etl.local-dir}")
  private String localDir;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    String fileName = "items_" + LocalDate.now() + ".csv";
    Path filePath = Path.of(localDir, fileName);

    String key = "raw/items/" + fileName;

    PutObjectRequest request = PutObjectRequest.builder()
      .bucket(bucket)
      .key(key)
      .contentType("text/csv")
      .build();

    s3Client.putObject(request, RequestBody.fromFile(filePath));

    return RepeatStatus.FINISHED;
  }
}
