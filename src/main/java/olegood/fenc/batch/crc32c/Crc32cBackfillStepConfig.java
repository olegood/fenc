package olegood.fenc.batch.crc32c;

import java.nio.file.Path;
import java.util.Map;
import olegood.fenc.checksum.ChecksumService;
import olegood.fenc.domain.Attachment;
import olegood.fenc.repository.AttachmentRepository;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class Crc32cBackfillStepConfig {

  private static final int chunkSize = 50;

  @Bean
  public Step crc32cBackfillStep(
      JobRepository jobRepository,
      PlatformTransactionManager txManager,
      ItemReader<Attachment> crc32cReader,
      ItemProcessor<Attachment, Attachment> crc32cProcessor,
      ItemWriter<Attachment> crc32cWriter) {

    return new StepBuilder("crc32cBackfillStep", jobRepository)
        .<Attachment, Attachment>chunk(50)
        .transactionManager(txManager)
        .reader(crc32cReader)
        .processor(crc32cProcessor)
        .writer(crc32cWriter)
        .build();
  }

  @Bean
  public RepositoryItemReader<Attachment> crc32cReader(AttachmentRepository repository) {
    return new RepositoryItemReaderBuilder<Attachment>()
        .name("crc32cReader")
        .repository(repository)
        .methodName("findByChecksumIsNull")
        .pageSize(chunkSize)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  public ItemProcessor<Attachment, Attachment> crc32cProcessor(ChecksumService checksumService) {
    return attachment -> {
      Path path = Path.of(attachment.getLocation());
      long crc32c = checksumService.compute(path);
      attachment.setChecksum(crc32c);
      return attachment;
    };
  }

  @Bean
  public ItemWriter<Attachment> crc32cWriter(AttachmentRepository repository) {
    return repository::saveAll;
  }
}
