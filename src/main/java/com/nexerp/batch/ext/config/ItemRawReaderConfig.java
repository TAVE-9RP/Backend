package com.nexerp.batch.ext.config;

import com.nexerp.batch.ext.dto.ItemRawRow;
import com.nexerp.batch.ext.repository.BatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ItemRawReaderConfig {

  private final BatchRepository batchRepository;

  @Bean
  public RepositoryItemReader<ItemRawRow> itemRawReader() {
    RepositoryItemReader<ItemRawRow> reader = new RepositoryItemReader<>();
    reader.setRepository(batchRepository);
    reader.setMethodName("findItemRawRows");
    reader.setPageSize(1000);
    reader.setSort(Map.of("id", Sort.Direction.ASC));
    return reader;
  }
}
