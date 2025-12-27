package olegood.fenc.batch.kek;

import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import olegood.fenc.crypto.DekService;
import olegood.fenc.crypto.KekService;
import olegood.fenc.domain.Attachment;
import olegood.fenc.repository.AttachmentRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class KekRotationStepConfig {

  private static final int chunkSize = 100;

  @Bean
  public Step kekRotationStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ItemReader<Attachment> attachmentReader,
      ItemProcessor<Attachment, Attachment> kekRotationProcessor,
      ItemWriter<Attachment> attachmentWriter) {

    return new StepBuilder("kekRotationStep", jobRepository)
        .<Attachment, Attachment>chunk(chunkSize)
        .transactionManager(transactionManager)
        .reader(attachmentReader)
        .processor(kekRotationProcessor)
        .writer(attachmentWriter)
        .build();
  }

  @Bean
  @StepScope
  public RepositoryItemReader<Attachment> attachmentReader(
      AttachmentRepository repository,
      @Value("#{jobParameters['oldKekAlias']}") String oldKekAlias) {
    return new RepositoryItemReaderBuilder<Attachment>()
        .name("attachmentReader")
        .repository(repository)
        .methodName("findByKekVersion")
        .arguments(List.of(oldKekAlias))
        .pageSize(chunkSize)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  @StepScope
  public ItemProcessor<Attachment, Attachment> kekRotationProcessor(
      KekService kekService,
      DekService dekService,
      @Value("#{jobParameters['oldKekAlias']}") String oldAlias,
      @Value("#{jobParameters['newKekAlias']}") String newAlias)
      throws Exception {

    SecretKey oldKek = kekService.loadKekByAlias(oldAlias);
    SecretKey newKek = kekService.loadKekByAlias(newAlias);

    return attachment -> {

      // Decrypt DEK with old KEK
      var dek = dekService.decryptDek(attachment.getEncryptedDek(), oldKek, attachment.getDekIv());

      // Encrypt DEK with new KEK
      var reEncryptedDek = dekService.encryptDek(dek, newKek, attachment.getDekIv());

      attachment.setEncryptedDek(reEncryptedDek);
      attachment.setKekVersion(newAlias);

      return attachment;
    };
  }

  @Bean
  public ItemWriter<Attachment> attachmentWriter(AttachmentRepository repository) {
    return repository::saveAll;
  }
}
