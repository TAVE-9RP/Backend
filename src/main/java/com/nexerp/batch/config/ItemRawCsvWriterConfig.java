package com.nexerp.batch.config;

import com.nexerp.batch.model.dto.ItemRawRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class ItemRawCsvWriterConfig {

  @Value("${etl.local-dir}")
  private String localDir;

  @Bean
  public FlatFileItemWriter<ItemRawRow> itemRawCsvWrite() throws Exception {
    Files.createDirectories(Path.of(localDir));

    String fileName = "items_" + LocalDate.now() + ".csv";
    Path filePath = Path.of(localDir, fileName);

    FieldExtractor<ItemRawRow> extractor = item -> new Object[] {
      item.getId(),
      item.getCompanyId(),
      item.getCode(),
      item.getQuantity(),
      item.getSafetyStock()
    };

    DelimitedLineAggregator<ItemRawRow> aggregator = new DelimitedLineAggregator<>();
    aggregator.setDelimiter(",");
    aggregator.setFieldExtractor(extractor);

    return new FlatFileItemWriterBuilder<ItemRawRow>()
      .name("itemRawCsvWriter")
      .resource(new FileSystemResource(filePath.toFile()))
      .headerCallback(w -> w.write("itemId,companyId,code,quantity,safetyStock"))
      .lineAggregator(aggregator)
      .build();
  }
}
