package olegood.fenc.batch.clerk;

import java.util.List;
import java.util.Map;
import olegood.fenc.crypto.kek.KeyStoreService;
import olegood.fenc.domain.Clerk;
import olegood.fenc.repository.ClerkRepository;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.data.RepositoryItemReader;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class ClerkReEncryptionJobConfig {

  @Bean
  public Job clerkKeyRotationJob(JobRepository jobRepository, Step clerkRotationStep) {
    return new JobBuilder("clerkKeyRotation", jobRepository).start(clerkRotationStep).build();
  }

  @Bean
  public Step clerkRotationStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      ItemReader<Clerk> clerkReader,
      ClerkReEncryptionProcessor processor,
      ItemWriter<Clerk> clerkWriter) {
    return new StepBuilder("clerkRotationStep", jobRepository)
        .<Clerk, Clerk>chunk(100)
        .transactionManager(transactionManager)
        .reader(clerkReader)
        .processor(processor)
        .writer(clerkWriter)
        .faultTolerant()
        .retryLimit(3)
        .retry(Exception.class)
        .build();
  }

  @Bean
  public RepositoryItemReader<Clerk> clerkReader(
      ClerkRepository repository, KeyStoreService keyStoreService) {
    return new RepositoryItemReaderBuilder<Clerk>()
        .name("clerkReader")
        .repository(repository)
        .methodName("findNotMatchingNewAlias")
        .arguments(List.of(keyStoreService.getActiveCekAlias()))
        .pageSize(100)
        .sorts(Map.of("id", Sort.Direction.ASC))
        .build();
  }

  @Bean
  public ItemWriter<Clerk> clerkWriter(ClerkRepository repository) {
    return repository::saveAll;
  }
}
