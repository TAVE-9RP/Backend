package com.nexerp.batch.job;

import com.nexerp.batch.model.dto.ItemRawRow;
import com.nexerp.batch.tasklet.S3UploadTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ItemReaderExportJobConfig {

  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;

  private final RepositoryItemReader<ItemRawRow> itemRawReader;
  private final FlatFileItemWriter<ItemRawRow> itemRawCsvWriter;
  private final S3UploadTasklet s3UploadTasklet;

  @Bean
  public Step exportItemRawCsvStep() {
    return new StepBuilder("exportItemRawCsvStep", jobRepository)
      .<ItemRawRow, ItemRawRow>chunk(1000, transactionManager)
      .reader(itemRawReader)
      .writer(itemRawCsvWriter)
      .build();
  }

  @Bean
  public Step uploadItemRawCsvToS3Step() {
    return new StepBuilder("uploadItemRawCsvToS3Step", jobRepository)
      .tasklet(s3UploadTasklet, transactionManager)
      .build();
  }

  @Bean
  public Job itemRawExportJob() {
    return new JobBuilder("itemRawExportJob", jobRepository)
      .incrementer(new RunIdIncrementer())
      .start(exportItemRawCsvStep())
      .next(uploadItemRawCsvToS3Step())
      .build();
  }
}
